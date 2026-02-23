@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalNativeApi::class,
    ExperimentalSerializationApi::class
)
@file:Suppress("unused")

package dev.dertyp

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.data.RpcEnvelope
import dev.dertyp.rpc.BaseRpcServiceManager
import dev.dertyp.rpc.dispatchService
import dev.dertyp.rpc.subscribeService
import dev.dertyp.serializers.AppCbor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.experimental.ExperimentalNativeApi
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class NativeRpcManager(client: HttpClient) : BaseRpcServiceManager(client) {
    var rpcUrl: String? = null
    var authToken: String? = null
    var refreshToken: String? = null
    var authenticated: Boolean = false
    var expiresAt: PlatformDate? = null

    public override suspend fun getRpcUrl(): String? = rpcUrl
    public override fun getAuthToken(): String? = authToken
    public override fun getRefreshToken(): String? = refreshToken
    public override fun isTokenExpired(): Boolean = expiresAt?.let {
        it.toEpochMilliseconds() < (currentTimeMillis() + 1.minutes.inWholeMilliseconds)
    } ?: true

    public override fun isAuthenticated(): Boolean = authenticated

    public override suspend fun updateAuth(response: AuthenticationResponse) {
        authToken = response.token
        refreshToken = response.refreshToken
        authenticated = true
        expiresAt = response.expiresAt
    }

    override suspend fun handleAuthFailure() {
        authenticated = false
        authToken = null
        refreshToken = null
    }

    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}

fun ByteArray.toNativeBuffer(outLen: CPointer<IntVar>): CPointer<ByteVar> {
    val ptr = nativeHeap.allocArray<ByteVar>(this.size)
    for (i in this.indices) {
        ptr[i] = this[i]
    }
    outLen.pointed.value = this.size
    return ptr
}

fun String.toNativeBuffer(): CPointer<ByteVar> {
    val bytes = this.encodeToByteArray()
    val ptr = nativeHeap.allocArray<ByteVar>(bytes.size + 1)
    for (i in bytes.indices) {
        ptr[i] = bytes[i]
    }
    ptr[bytes.size] = 0
    return ptr
}

@CName("common_rpc_manager_create")
fun createManager(): COpaquePointer {
    val client = HttpClient(CIO) {
        install(UserAgent) {
            agent = "Synara/Rust"
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }
        install(WebSockets) {
            pingInterval = 15.seconds
            maxFrameSize = Long.MAX_VALUE
        }
        install(Krpc) {
            serialization {
                cbor(AppCbor)
            }
        }
    }
    val manager = NativeRpcManager(client)
    return StableRef.create(manager).asCPointer()
}

@CName("common_rpc_manager_release")
fun releaseManager(ptr: COpaquePointer) {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    manager.coroutineScope.cancel()
    ptr.asStableRef<NativeRpcManager>().dispose()
}

@CName("common_rpc_free_buffer")
fun freeBuffer(ptr: CPointer<ByteVar>) {
    nativeHeap.free(ptr)
}

@CName("common_rpc_call")
fun commonRpcCall(
    ptr: COpaquePointer,
    serviceName: CPointer<ByteVar>,
    methodName: CPointer<ByteVar>,
    argsPtr: CPointer<ByteVar>?,
    argsLen: Int,
    outLen: CPointer<IntVar>
): CPointer<ByteVar> {
    val envelope = try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        val service = serviceName.toKString()
        val method = methodName.toKString()
        val args = argsPtr?.readBytes(argsLen) ?: byteArrayOf()
        runBlocking {
            val result = dispatchService(manager, service, method, args)
            RpcEnvelope(data = result)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        RpcEnvelope(error = e.message ?: e.toString())
    }
    val encoded = AppCbor.encodeToByteArray(envelope)
    return encoded.toNativeBuffer(outLen)
}

typealias FlowCallback = CPointer<CFunction<(COpaquePointer?, CPointer<ByteVar>?, Int) -> Unit>>

@CName("common_rpc_subscribe")
fun commonRpcSubscribe(
    ptr: COpaquePointer,
    serviceName: CPointer<ByteVar>,
    methodName: CPointer<ByteVar>,
    argsPtr: CPointer<ByteVar>?,
    argsLen: Int,
    context: COpaquePointer?,
    onEach: FlowCallback
): COpaquePointer? {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        val service = serviceName.toKString()
        val method = methodName.toKString()
        val args = argsPtr?.readBytes(argsLen) ?: byteArrayOf()

        val job = subscribeService(manager.coroutineScope, manager, service, method, args) { data: ByteArray ->
            memScoped {
                val dataPtr = data.toCValues().ptr
                onEach.invoke(context, dataPtr, data.size)
            }
        }

        StableRef.create(job).asCPointer()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@CName("common_rpc_unsubscribe")
fun commonRpcUnsubscribe(jobPtr: COpaquePointer) {
    try {
        val job = jobPtr.asStableRef<Job>().get()
        job.cancel()
        jobPtr.asStableRef<Job>().dispose()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@CName("common_rpc_set_url")
fun setUrl(ptr: COpaquePointer, url: CPointer<ByteVar>) {
    try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        manager.rpcUrl = url.toKString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@CName("common_rpc_get_url")
fun getUrl(ptr: COpaquePointer): CPointer<ByteVar>? {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        runBlocking {
            manager.getRpcUrl()?.toNativeBuffer()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@CName("common_rpc_validate_server")
fun validateServer(ptr: COpaquePointer, url: CPointer<ByteVar>): Boolean {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        runBlocking {
            manager.validateServer(url.toKString())
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@CName("common_rpc_update_auth")
fun updateAuth(ptr: COpaquePointer, argsPtr: CPointer<ByteVar>, argsLen: Int) {
    try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        val args = argsPtr.readBytes(argsLen)
        val response = AppCbor.decodeFromByteArray<AuthenticationResponse>(args)
        runBlocking {
            manager.updateAuth(response)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@CName("common_rpc_is_authenticated")
fun isAuthenticated(ptr: COpaquePointer): Boolean {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        manager.isAuthenticated()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@CName("common_rpc_get_auth_token")
fun getAuthToken(ptr: COpaquePointer): CPointer<ByteVar>? {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        manager.getAuthToken()?.toNativeBuffer()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@CName("common_rpc_get_refresh_token")
fun getRefreshToken(ptr: COpaquePointer): CPointer<ByteVar>? {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        manager.getRefreshToken()?.toNativeBuffer()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@CName("common_rpc_is_token_expired")
fun isTokenExpired(ptr: COpaquePointer): Boolean {
    return try {
        val manager = ptr.asStableRef<NativeRpcManager>().get()
        manager.isTokenExpired()
    } catch (e: Exception) {
        e.printStackTrace()
        true
    }
}
