package dev.dertyp.rpc

import dev.dertyp.core.prefixIfNotBlank
import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.ioDispatcher
import dev.dertyp.services.IAuthService
import dev.dertyp.services.IServerStatsService
import dev.dertyp.services.IUserService
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.util.collections.ConcurrentMap
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.rpc.RpcClient
import kotlinx.rpc.annotations.Rpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.withService
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

abstract class BaseRpcServiceManager(
    protected val client: HttpClient,
    protected val scope: CoroutineScope = CoroutineScope(ioDispatcher + SupervisorJob())
) {
    private var _servicesClient: RpcClient? = null
    protected val mutex = Mutex()
    protected val serviceCache = ConcurrentMap<KClass<*>, Any>()

    private val _isServerReachable = MutableStateFlow(true)
    val isServerReachable: StateFlow<Boolean> = _isServerReachable.asStateFlow()

    protected abstract suspend fun getRpcUrl(): String?
    protected abstract fun getAuthToken(): String?
    protected abstract fun getRefreshToken(): String?
    protected abstract fun isTokenExpired(): Boolean
    protected abstract fun isAuthenticated(): Boolean
    protected abstract suspend fun updateAuth(response: AuthenticationResponse)
    protected abstract suspend fun handleAuthFailure()
    
    protected open fun onServerUnreachable() {
        _isServerReachable.value = false
    }
    
    protected open fun onServerReachable() {
        _isServerReachable.value = true
    }

    protected val transparentClient = ReconnectingRpcClient(
        delegateProvider = {
            try {
                getAuthenticatedClient().also {
                    onServerReachable()
                }
            } catch (e: Exception) {
                if (e is IllegalStateException && !isAuthenticated()) {
                    handleAuthFailure()
                }
                throw e
            }
        },
        onCancel = { 
            scope.launch { clear() } 
        },
        onFailure = { onServerUnreachable() }
    )

    private fun createReconnectingClient(
        baseUrl: String?,
        endpoint: String = "",
        token: String? = null
    ): RpcClient {
        return client.reconnectingRpcClient(
            onCancel = {
                scope.launch { clear() }
            },
            onFailure = {
                onServerUnreachable()
            }
        ) {
            url("${baseUrl}/rpc${endpoint.prefixIfNotBlank("/")}")
            //header(SynaraPackHeader, "true")
            token?.let { header("Authorization", "Bearer $it") }
        }
    }

    suspend fun getAuthService(): IAuthService = withContext(ioDispatcher) {
        val baseUrl = getRpcUrl()
        val rpcClient = createReconnectingClient(baseUrl, "auth")
        rpcClient.withService<IAuthService>()
    }

    suspend fun getServerStatsService(): IServerStatsService = withContext(ioDispatcher) {
        val baseUrl = getRpcUrl()
        val rpcClient = createReconnectingClient(baseUrl)
        rpcClient.withService<IServerStatsService>()
    }

    suspend fun validateServer(baseUrl: String): Boolean = withContext(ioDispatcher) {
        try {
            val rpcClient = createReconnectingClient(baseUrl)
            val statsService = rpcClient.withService<IServerStatsService>()

            statsService.health()
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun ensureAuthenticated() {
        if (!isTokenExpired()) return

        val result: Any? = mutex.withLock {
            if (!isTokenExpired()) return@withLock null

            val refreshToken = getRefreshToken()
            if (refreshToken != null) {
                try {
                    val authService = getAuthService()
                    authService.refreshToken(refreshToken)
                } catch (e: Exception) {
                    e
                }
            } else if (isAuthenticated()) {
                IllegalStateException("Session expired and no refresh token available")
            } else {
                null
            }
        }

        if (result is AuthenticationResponse) {
            clear()
            updateAuth(result)
        } else if (result is Exception) {
            handleAuthFailure()
            throw result
        }
    }

    suspend fun getAuthenticatedClient(): RpcClient {
        val cached = _servicesClient
        if (cached != null && !isTokenExpired()) return cached

        ensureAuthenticated()

        var authException: Exception? = null
        val rpcClient = try {
            mutex.withLock {
                _servicesClient?.let { return@withLock it }

                val baseUrl = getRpcUrl()
                val token = getAuthToken() ?: throw IllegalStateException("Not authenticated")

                var lastException: Exception? = null
                for (attempt in 1..3) {
                    try {
                        val rpcClientInstance = client.rpc {
                            url("${baseUrl}/rpc/services")
                            //header(SynaraPackHeader, "true")
                            header("Authorization", "Bearer $token")
                        }
                        rpcClientInstance.withService<IUserService>().me()
                        _servicesClient = rpcClientInstance
                        return@withLock rpcClientInstance
                    } catch (e: Exception) {
                        lastException = e
                        if (isAuthException(e)) {
                            throw e
                        }
                        when (e) {
                            is ConnectTimeoutException,
                            is IOException,
                            is UnresolvedAddressException -> {
                                delay((1000L * attempt).milliseconds)
                                continue
                            }

                            else -> throw e
                        }
                    }
                }
                onServerUnreachable()
                throw lastException ?: IllegalStateException("Failed to connect after retries")
            }
        } catch (e: Exception) {
            if (isAuthException(e)) {
                authException = e
                null
            } else {
                throw e
            }
        }

        if (authException != null) {
            handleAuthFailure()
            throw authException
        }

        return rpcClient!!
    }

    protected open fun isAuthException(e: Exception): Boolean {
        return when (e) {
            is WebSocketException if e.message?.contains("401") == true -> true
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <@Rpc T : Any> getService(serviceClass: KClass<T>): T {
        return serviceCache.getOrPut(serviceClass) {
            transparentClient.withService(serviceClass)
        } as T
    }

    inline fun <@Rpc reified T : Any> getService(): T {
        return getService(T::class)
    }

    suspend fun clear() {
        mutex.withLock {
            val oldClient = _servicesClient
            _servicesClient = null
            try {
                (oldClient as? AutoCloseable)?.close()
            } catch (_: Exception) {
            }
            serviceCache.clear()
        }
    }
}
