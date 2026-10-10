package dev.dertyp.rpc

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.data.ServerValidationResult
import dev.dertyp.serializers.AppCbor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.ServerSocket
import java.security.GeneralSecurityException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalSerializationApi::class)
class ValidateServerTlsTest {

    class StoringRpcManager(client: HttpClient) : BaseRpcServiceManager(client) {
        var url: String? = null
        val setRpcUrlCalls = mutableListOf<String>()

        override val sslConfirmed: Boolean = false
        override suspend fun setSslConfirmed(value: Boolean) {}
        override suspend fun getRpcUrl(): String? = url
        override suspend fun setRpcUrl(host: String, port: Int, ssl: Boolean, path: String) {
            setRpcUrlCalls += "$host:$port:$ssl:$path"
        }

        override fun getAuthToken(): String? = null
        override fun getRefreshToken(): String? = null
        override fun isTokenExpired(): Boolean = false
        override fun isAuthenticated(): Boolean = false
        override suspend fun updateAuth(response: AuthenticationResponse) {}
        override suspend fun handleAuthFailure(reason: Throwable?) {}
        override fun isSslException(e: Throwable): Boolean =
            generateSequence(e) { it.cause }.any { it is GeneralSecurityException }
    }

    private fun rpcClient(trustServer: Boolean = false) = HttpClient(CIO) {
        install(WebSockets)
        install(Krpc) { serialization { cbor(AppCbor) } }
        if (trustServer) {
            engine {
                https {
                    trustManager = HealthRpcServer.trustManager()
                    serverName = "localhost"
                }
            }
        }
    }

    @Test
    fun aTlsFailureWithWorkingPlaintextReportsPlaintextAndTheTlsFailure() = runBlocking {
        HealthRpcServer(tls = true).use { server ->
            rpcClient().use { client ->
                val manager = StoringRpcManager(client)

                val result = manager.validateServer("127.0.0.1", server.port)

                assertEquals(ServerValidationResult(validated = true, useSsl = false, tlsFailed = true), result)
                assertEquals(emptyList(), manager.setRpcUrlCalls)
                assertNull(manager.sessionSslOverride.value)
            }
        }
    }

    @Test
    fun aServerStoredSecureAfterSuchAValidationRunsOnTheSessionOverride() = runBlocking {
        HealthRpcServer(tls = true).use { server ->
            rpcClient().use { client ->
                val manager = StoringRpcManager(client)
                assertEquals(true, manager.validateServer("127.0.0.1", server.port).tlsFailed)
                manager.url = "wss://127.0.0.1:${server.port}"

                assertFalse(manager.checkSslSupport())

                assertEquals(false, manager.sessionSslOverride.value)
                assertEquals(emptyList(), manager.setRpcUrlCalls)
            }
        }
    }

    @Test
    fun aServerWithoutTlsReportsPlaintextWithoutATlsFailure() = runBlocking {
        HealthRpcServer(tls = false).use { server ->
            rpcClient().use { client ->
                val result = StoringRpcManager(client).validateServer("127.0.0.1", server.port)

                assertEquals(ServerValidationResult(validated = true, useSsl = false, tlsFailed = false), result)
            }
        }
    }

    @Test
    fun workingTlsReportsSslWithoutATlsFailure() = runBlocking {
        HealthRpcServer(tls = true).use { server ->
            rpcClient(trustServer = true).use { client ->
                val result = StoringRpcManager(client).validateServer("127.0.0.1", server.port)

                assertEquals(ServerValidationResult(validated = true, useSsl = true, tlsFailed = false), result)
            }
        }
    }

    @Test
    fun aPlaintextOnlyValidationNeverTriesTlsAndReportsNoTlsFailure() = runBlocking {
        HealthRpcServer(tls = true).use { server ->
            rpcClient().use { client ->
                val result = StoringRpcManager(client).validateServer("127.0.0.1", server.port, useSsl = false)

                assertEquals(ServerValidationResult(validated = true, useSsl = false, tlsFailed = false), result)
            }
        }
    }

    @Test
    fun anUnreachableServerIsNotValidatedAndReportsNoTlsFailure() = runBlocking {
        rpcClient().use { client ->
            val port = ServerSocket(0).use { it.localPort }

            val result = StoringRpcManager(client).validateServer("127.0.0.1", port)

            assertEquals(ServerValidationResult(validated = false, useSsl = false), result)
        }
    }

    @Test
    fun theResultKeepsItsTwoArgumentShape() {
        val result = ServerValidationResult(validated = true, useSsl = true)

        assertFalse(result.tlsFailed)
        assertEquals(ServerValidationResult(true, true, false), result)
    }
}
