package dev.dertyp.rpc

import dev.dertyp.data.AuthenticationResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.runBlocking
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SslFailureKeepsStoredUrlTest {

    class RecordingRpcManager(
        client: HttpClient,
        private val url: String,
        override val sslConfirmed: Boolean,
    ) : BaseRpcServiceManager(client) {
        val setRpcUrlCalls = mutableListOf<String>()
        val setSslConfirmedCalls = mutableListOf<Boolean>()

        override suspend fun getRpcUrl(): String = url
        override suspend fun setRpcUrl(host: String, port: Int, ssl: Boolean, path: String) {
            setRpcUrlCalls += "$host:$port:$ssl:$path"
        }

        override suspend fun setSslConfirmed(value: Boolean) {
            setSslConfirmedCalls += value
        }

        override fun getAuthToken(): String? = null
        override fun getRefreshToken(): String? = null
        override fun isTokenExpired(): Boolean = false
        override fun isAuthenticated(): Boolean = false
        override suspend fun updateAuth(response: AuthenticationResponse) {}
        override suspend fun handleAuthFailure(reason: Throwable?) {}
        override fun isSslException(e: Throwable): Boolean = true
    }

    private fun assertSessionOverrideOnly(sslConfirmed: Boolean) = runBlocking {
        HttpClient(CIO).use { client ->
            val port = ServerSocket(0).use { it.localPort }
            val manager = RecordingRpcManager(client, "wss://127.0.0.1:$port", sslConfirmed)
            assertNull(manager.sessionSslOverride.value)

            assertFalse(manager.checkSslSupport())

            assertEquals(false, manager.sessionSslOverride.value)
            assertEquals(emptyList(), manager.setRpcUrlCalls)
            assertEquals(emptyList(), manager.setSslConfirmedCalls)

            assertTrue(manager.checkSslSupport())
            assertEquals(false, manager.sessionSslOverride.value)
            assertEquals(emptyList(), manager.setRpcUrlCalls)

            manager.resetSslSession()
            assertNull(manager.sessionSslOverride.value)
            assertFalse(manager.checkSslSupport())
            assertEquals(emptyList(), manager.setRpcUrlCalls)
        }
    }

    @Test
    fun tlsFailureBeforeConfirmationSetsOnlyTheSessionOverride() = assertSessionOverrideOnly(sslConfirmed = false)

    @Test
    fun tlsFailureAfterConfirmationSetsOnlyTheSessionOverride() = assertSessionOverrideOnly(sslConfirmed = true)
}
