package dev.dertyp.rpc

import dev.dertyp.serializers.AppCbor
import kotlinx.rpc.internal.utils.InternalRpcApi
import kotlinx.rpc.krpc.internal.KrpcCallMessage
import kotlinx.rpc.krpc.internal.KrpcMessage
import kotlinx.rpc.krpc.internal.KrpcPlugin
import kotlinx.rpc.krpc.internal.KrpcProtocolMessage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PushbackInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.thread

@OptIn(InternalRpcApi::class, ExperimentalSerializationApi::class)
class HealthRpcServer(private val tls: Boolean) : AutoCloseable {

    private val server = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
    val port: Int get() = server.localPort

    init {
        thread(isDaemon = true) {
            while (!server.isClosed) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                thread(isDaemon = true) { socket.use { runCatching { accept(it) } } }
            }
        }
    }

    override fun close() = server.close()

    private fun accept(socket: Socket) {
        val input = PushbackInputStream(socket.getInputStream())
        val first = input.read()
        if (first != TLS_HANDSHAKE_RECORD) {
            input.unread(first)
            serve(input, socket.getOutputStream())
            return
        }
        if (!tls) {
            socket.getOutputStream().write("HTTP/1.1 400 Bad Request\r\nConnection: close\r\nContent-Length: 0\r\n\r\n".toByteArray())
            return
        }
        val secure = serverContext().socketFactory
            .createSocket(socket, ByteArrayInputStream(byteArrayOf(first.toByte())), true) as SSLSocket
        secure.use {
            it.startHandshake()
            serve(it.inputStream, it.outputStream)
        }
    }

    private fun serve(input: InputStream, output: OutputStream) {
        val key = readRequestHeaders(input)["sec-websocket-key"]
        if (key == null) {
            output.write("HTTP/1.1 404 Not Found\r\nConnection: close\r\nContent-Length: 0\r\n\r\n".toByteArray())
            return
        }
        val accept = Base64.getEncoder()
            .encodeToString(MessageDigest.getInstance("SHA-1").digest((key + WEBSOCKET_GUID).toByteArray()))
        output.write(
            "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: $accept\r\n\r\n"
                .toByteArray()
        )
        output.flush()

        val frames = DataInputStream(input)
        while (true) {
            val opcode = frames.readUnsignedByte() and 0x0F
            val payload = readPayload(frames)
            when (opcode) {
                OPCODE_BINARY -> answer(AppCbor.decodeFromByteArray(KrpcMessage.serializer(), payload), output)
                OPCODE_PING -> writeFrame(output, OPCODE_PONG, payload)
                OPCODE_CLOSE -> {
                    writeFrame(output, OPCODE_CLOSE, payload)
                    return
                }
            }
        }
    }

    private fun answer(message: KrpcMessage, output: OutputStream) {
        val reply: KrpcMessage = when (message) {
            is KrpcProtocolMessage.Handshake -> KrpcProtocolMessage.Handshake(KrpcPlugin.ALL - KrpcPlugin.BACKPRESSURE)
            is KrpcCallMessage.CallData -> KrpcCallMessage.CallSuccessBinary(
                callId = message.callId,
                serviceType = message.serviceType,
                data = AppCbor.encodeToByteArray(Boolean.serializer(), true),
                serviceId = message.serviceId,
            )

            else -> return
        }
        writeFrame(output, OPCODE_BINARY, AppCbor.encodeToByteArray(KrpcMessage.serializer(), reply))
    }

    private fun readRequestHeaders(input: InputStream): Map<String, String> {
        val request = StringBuilder()
        while (!request.endsWith("\r\n\r\n")) {
            val next = input.read()
            if (next < 0) break
            request.append(next.toChar())
        }
        return request.lines().drop(1).filter { ':' in it }.associate {
            it.substringBefore(':').trim().lowercase() to it.substringAfter(':').trim()
        }
    }

    private fun readPayload(frames: DataInputStream): ByteArray {
        val lengthByte = frames.readUnsignedByte()
        val length = when (val short = lengthByte and 0x7F) {
            126 -> frames.readUnsignedShort()
            127 -> frames.readLong().toInt()
            else -> short
        }
        val mask = ByteArray(if (lengthByte and 0x80 != 0) 4 else 0).also { frames.readFully(it) }
        val payload = ByteArray(length).also { frames.readFully(it) }
        if (mask.isNotEmpty()) {
            for (i in payload.indices) payload[i] = (payload[i].toInt() xor mask[i % 4].toInt()).toByte()
        }
        return payload
    }

    private fun writeFrame(output: OutputStream, opcode: Int, payload: ByteArray) {
        output.write(0x80 or opcode)
        if (payload.size < 126) {
            output.write(payload.size)
        } else {
            output.write(126)
            output.write(payload.size shr 8)
            output.write(payload.size and 0xFF)
        }
        output.write(payload)
        output.flush()
    }

    companion object {
        private const val TLS_HANDSHAKE_RECORD = 0x16
        private const val OPCODE_BINARY = 0x2
        private const val OPCODE_CLOSE = 0x8
        private const val OPCODE_PING = 0x9
        private const val OPCODE_PONG = 0xA
        private const val WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        private const val KEYSTORE_PASSWORD = "changeit"
        private const val SELF_SIGNED_LOCALHOST_PKCS12 =
            "MIID9gIBAzCCA6AGCSqGSIb3DQEHAaCCA5EEggONMIIDiTCCASAGCSqGSIb3DQEHAaCCAREEggENMIIBCTCCAQUGCyqGSIb3DQEMCgECoIG9MIG6MGYGCSqGSIb3DQEFDTBZMDgGCSqGSIb3DQEFDDArBBSKB7hnGxJ3j9QbRJ3lLSJ9YUGN6QICJxACASAwDAYIKoZIhvcNAgkFADAdBglghkgBZQMEASoEEN3Y+/6+KS+xxgOMDJUrBE8EUDgE3qjgddh+0Aa0tPO5BRyyMPR7r0QJcLr1dyjvc0fxCAXe7+AzPH+k/adAcUbKc4m29njYp1O+tHZ77A2Faa3xHem9GC5IOZuivQ+lmb0DMTYwEQYJKoZIhvcNAQkUMQQeAgB0MCEGCSqGSIb3DQEJFTEUBBJUaW1lIDE3OTE1OTkwMjk1MjkwggJhBgkqhkiG9w0BBwagggJSMIICTgIBADCCAkcGCSqGSIb3DQEHATBmBgkqhkiG9w0BBQ0wWTA4BgkqhkiG9w0BBQwwKwQUJbASlN4PRzaRrKH14juClc6jPP4CAicQAgEgMAwGCCqGSIb3DQIJBQAwHQYJYIZIAWUDBAEqBBA4CnjY65K93AXdteoxwZaogIIB0FOU9djWv3CznbSl2iCJbtV22i+t6nBSEswijZ4M7wIXkJCTKYwKfiwwkbxbJEheNaHiLDFwwgFVCf8YRACjS4b4A2djpyjePAYj0JBkOHKuMkikKVbZ4j7fgsYOPx5zPkrhN3furfnICvIMTsArvOPwjzLcqRDl7P3st67tK0AnpLBTgzt3M0meakFjgK3lwY+wrCPiRl/vQUJNo1Q1sPFtKQ3Hy92XXcIdWbX6cpy/XLALhmsQOlhLkuVFCVb/uqkRCQKwqc/f2jHwfw5suf3q+nHW0FKTaq/SQ8buZuqHY1A/MfCBLiMKYW1gWzHjhGBWkFL7vCHf2fcjI2h1MCqJ6vr6w2eFeDaWMfcOFotIKVT1D1wwXxI6NbvPixRNEl/VkraeLOylmZh1mEb29nThA7SpAdMNl4U9Bf6pV9PmCrbvZpRbCK5N4kkWDLlNt51fvVUK9GKTMbT3BN71cJyV2Ab3Fij5ZL1N/a7GQxIdenu8caq13oXVfAKNSfGejXIJeacssAG3otP/drICqfTID9j77k1MNX1FwJ34Ec1L188EvX7OFUGhXAm4y9gLxH4HOXDscio5XbIQch5XbXEGJmBcF3SJmcOPIxXEoXCHME0wMTANBglghkgBZQMEAgEFAAQgKPebOfVtUre5glL5M5mSTgnDOwQJJ7iN2MPHtzC6ypsEFBUfi4wIjZ6nfYSYWN+VEHKRUwwcAgInEA=="

        private fun keyStore(): KeyStore = KeyStore.getInstance("PKCS12").apply {
            load(ByteArrayInputStream(Base64.getDecoder().decode(SELF_SIGNED_LOCALHOST_PKCS12)), KEYSTORE_PASSWORD.toCharArray())
        }

        private fun serverContext(): SSLContext {
            val keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keys.init(keyStore(), KEYSTORE_PASSWORD.toCharArray())
            return SSLContext.getInstance("TLS").apply { init(keys.keyManagers, null, null) }
        }

        fun trustManager(): X509TrustManager {
            val trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trust.init(keyStore())
            return trust.trustManagers.filterIsInstance<X509TrustManager>().single()
        }
    }
}
