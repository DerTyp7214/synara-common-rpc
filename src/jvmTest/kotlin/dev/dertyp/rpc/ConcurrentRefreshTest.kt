package dev.dertyp.rpc

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.platformDateFromEpochMilliseconds
import dev.dertyp.services.IAuthService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentRefreshTest {

    private class FakeAuthService : IAuthService {
        val refreshCalls = CopyOnWriteArrayList<String>()
        private val counter = AtomicInteger(0)

        override suspend fun authenticate(username: String, password: String): AuthenticationResponse =
            error("unused")

        override suspend fun refreshToken(refreshToken: String): AuthenticationResponse {
            refreshCalls += refreshToken
            delay(50)
            val n = counter.incrementAndGet()
            return AuthenticationResponse("access-$n", "refresh-$n", platformDateFromEpochMilliseconds(Long.MAX_VALUE / 2))
        }

        override suspend fun createDeviceSession(userAgent: String): AuthenticationResponse = error("unused")
    }

    private class TokenRpcManager(client: HttpClient, private val authService: IAuthService) : BaseRpcServiceManager(client) {
        @Volatile var token: String? = "access-0"
        @Volatile var refresh: String? = "refresh-0"

        override val sslConfirmed: Boolean = false
        override suspend fun setSslConfirmed(value: Boolean) {}
        override suspend fun getRpcUrl(): String? = null
        override suspend fun setRpcUrl(host: String, port: Int, ssl: Boolean, path: String) {}

        override fun getAuthToken(): String? = token
        override fun getRefreshToken(): String? = refresh
        override fun isTokenExpired(): Boolean = false
        override fun isAuthenticated(): Boolean = token != null
        override suspend fun updateAuth(response: AuthenticationResponse) {
            token = response.token
            refresh = response.refreshToken
        }
        override suspend fun handleAuthFailure(reason: Throwable?) {}
        override suspend fun getAuthService(): IAuthService = authService
    }

    @Test
    fun `concurrent forced refreshes send one refresh request`() = runBlocking {
        HttpClient(CIO).use { client ->
            val authService = FakeAuthService()
            val manager = TokenRpcManager(client, authService)

            val tokens = (1..10).map {
                async(Dispatchers.Default) { manager.accessToken(forceRefresh = true) }
            }.awaitAll()

            assertEquals(listOf("refresh-0"), authService.refreshCalls)
            assertEquals(List(10) { "access-1" }, tokens)
        }
    }

    @Test
    fun `forced refresh after a completed refresh refreshes again`() = runBlocking {
        HttpClient(CIO).use { client ->
            val authService = FakeAuthService()
            val manager = TokenRpcManager(client, authService)

            assertEquals("access-1", manager.accessToken(forceRefresh = true))
            assertEquals("access-2", manager.accessToken(forceRefresh = true))
            assertEquals(listOf("refresh-0", "refresh-1"), authService.refreshCalls)
        }
    }
}
