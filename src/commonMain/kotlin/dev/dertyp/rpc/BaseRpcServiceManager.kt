@file:Suppress("unused")

package dev.dertyp.rpc

import dev.dertyp.core.ConcurrentMutableMap
import dev.dertyp.core.prefixIfNotBlank
import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.data.HandshakeResponse
import dev.dertyp.data.ServerValidationResult
import dev.dertyp.ioDispatcher
import dev.dertyp.services.IAuthService
import dev.dertyp.services.IHandshakeService
import dev.dertyp.services.IServerStatsService
import dev.dertyp.services.IUserService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.Url
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
import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.withService
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class BaseRpcServiceManager(
    protected val client: HttpClient,
    baseScope: CoroutineScope = CoroutineScope(ioDispatcher + SupervisorJob()),
) {
    protected val scope: CoroutineScope = CoroutineScope(
        baseScope.coroutineContext + CoroutineExceptionHandler { _, e ->
            when (e) {
                is IOException,
                is ConnectTimeoutException,
                is UnresolvedAddressException -> onServerUnreachable()
                else -> Unit
            }
        }
    )
    private var _servicesClient: KtorRpcClient? = null
    protected val mutex = Mutex()
    protected val serviceCache = ConcurrentMutableMap<KClass<*>, Any>()

    open val supportsSsl: Boolean = true
    private var sslChecked = false

    private val _isServerReachable = MutableStateFlow(true)
    val isServerReachable: StateFlow<Boolean> = _isServerReachable.asStateFlow()

    protected abstract suspend fun getRpcUrl(): String?
    protected abstract suspend fun setRpcUrl(host: String, port: Int, ssl: Boolean, path: String = "/")
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
            getAuthenticatedClient().also {
                onServerReachable()
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
        checkSslSupport()
        val baseUrl = getRpcUrl()
        val rpcClient = createReconnectingClient(baseUrl, "auth")
        rpcClient.withService<IAuthService>()
    }

    suspend fun getServerStatsService(): IServerStatsService = withContext(ioDispatcher) {
        checkSslSupport()
        val baseUrl = getRpcUrl()
        val rpcClient = createReconnectingClient(baseUrl)
        rpcClient.withService<IServerStatsService>()
    }

    suspend fun getHandshakeService(): IHandshakeService = withContext(ioDispatcher) {
        val baseUrl = getRpcUrl()
        val rpcClient = createReconnectingClient(baseUrl)
        rpcClient.withService<IHandshakeService>()
    }

    @Suppress("HttpUrlsUsage")
    suspend fun checkSslSupport(): Boolean = withContext(ioDispatcher) {
        if (!supportsSsl || sslChecked) return@withContext true
        val baseUrl = getRpcUrl() ?: return@withContext true
        if (!baseUrl.startsWith("https://") && !baseUrl.startsWith("wss://")) return@withContext true

        try {
            val handshakeUrl = baseUrl.replace("wss://", "https://").replace("ws://", "http://")
            val response = client.get("${handshakeUrl}/handshake").body<HandshakeResponse>()
            sslChecked = true
            
            if (!response.secure) {
                val url = Url(baseUrl)
                setRpcUrl(url.host, url.port, false, url.encodedPath)
                clear()
                return@withContext false
            }
            true
        } catch (e: Throwable) {
            if (isSslException(e)) {
                val url = Url(baseUrl)
                setRpcUrl(url.host, url.port, false, url.encodedPath)
                clear()
                sslChecked = true
                return@withContext false
            }
            true
        }
    }

    suspend fun validateServer(
        host: String,
        port: Int,
        path: String = "/",
        useSsl: Boolean = true
    ): ServerValidationResult = withContext(ioDispatcher) {
        val schemes = if (useSsl) listOf("wss", "ws") else listOf("ws")

        for (s in schemes) {
            try {
                val formattedPath = path.prefixIfNotBlank("/").removeSuffix("/")
                val safePath = formattedPath.replace("//", "/")
                val baseUrl = "$s://$host:$port$safePath"

                val result = withTimeoutOrNull(5.seconds) {
                    val rpcClient = client.rpc {
                        url("$baseUrl/rpc")
                    }

                    try {
                        val statsService = rpcClient.withService<IServerStatsService>()
                        if (statsService.health()) {
                            ServerValidationResult(validated = true, useSsl = s == "wss")
                        } else null
                    } finally {
                        rpcClient.close()
                    }
                }
                if (result != null) return@withContext result
            } catch (_: Throwable) {
            }
        }
        ServerValidationResult(validated = false, useSsl = false)
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
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    e
                }
            } else if (isAuthenticated()) {
                SessionExpiredException()
            } else {
                null
            }
        }

        if (result is AuthenticationResponse) {
            clear()
            updateAuth(result)
        } else if (result is Throwable) {
            val genuineRejection = result is SessionExpiredException ||
                isAuthException(result) ||
                !isTransportException(result)
            if (genuineRejection) {
                onAuthFailure()
            } else {
                onServerUnreachable()
            }
            throw result
        }
    }

    suspend fun getAuthenticatedClient(): KtorRpcClient {
        val cached = _servicesClient
        if (cached != null && !isTokenExpired()) return cached

        checkSslSupport()

        ensureAuthenticated()

        var authException: Throwable? = null
        val rpcClient = try {
            mutex.withLock {
                _servicesClient?.let { return@withLock it }

                val baseUrl = getRpcUrl()
                val token = getAuthToken() ?: throw IllegalStateException("Not authenticated")

                var lastException: Throwable? = null
                for (attempt in 1..3) {
                    try {
                        val rpcClientInstance = withContext(scope.coroutineContext) {
                            client.rpc {
                                url("${baseUrl}/rpc/services")
                                header("Authorization", "Bearer $token")
                            }
                        }
                        rpcClientInstance.withService<IUserService>().me()
                        _servicesClient = rpcClientInstance
                        return@withLock rpcClientInstance
                    } catch (e: Throwable) {
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
        } catch (e: Throwable) {
            if (isAuthException(e)) {
                authException = e
                null
            } else {
                throw e
            }
        }

        if (authException != null) {
            onAuthFailure()
            throw authException
        }

        return rpcClient!!
    }

    open suspend fun getDedicatedClient(): KtorRpcClient {
        checkSslSupport()
        ensureAuthenticated()
        
        var authException: Throwable?
        try {
            val baseUrl = getRpcUrl()
            val token = getAuthToken() ?: throw IllegalStateException("Not authenticated")

            var lastException: Throwable? = null
            for (attempt in 1..3) {
                try {
                    return withContext(scope.coroutineContext) {
                        client.rpc {
                            url("${baseUrl}/rpc/services")
                            header("Authorization", "Bearer $token")
                        }
                    }
                } catch (e: Throwable) {
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
            throw lastException ?: IllegalStateException("Failed to connect dedicated client after retries")
        } catch (e: Throwable) {
            if (isAuthException(e)) {
                authException = e
            } else {
                throw e
            }
        }

        onAuthFailure()
        throw authException
    }

    private val authFailureMutex = Mutex()

    protected suspend fun onAuthFailure() {
        authFailureMutex.withLock {
            if (!isAuthenticated()) return
            handleAuthFailure()
        }
    }

    protected open fun isAuthException(e: Throwable): Boolean {
        return when (e) {
            is WebSocketException if e.message?.contains("401") == true -> true
            else -> false
        }
    }

    protected open fun isTransportException(e: Throwable): Boolean = when (e) {
        is IOException,
        is ConnectTimeoutException,
        is HttpRequestTimeoutException,
        is UnresolvedAddressException -> true
        is WebSocketException -> !isAuthException(e)
        else -> e.cause?.let { isTransportException(it) } == true
    }

    protected open fun isSslException(e: Throwable): Boolean {
        val message = e.message?.lowercase() ?: ""
        return message.contains("ssl") || 
               message.contains("tls") || 
               message.contains("certificate") || 
               message.contains("handshake failed")
    }

    @Suppress("UNCHECKED_CAST")
    open fun <@Rpc T : Any> getService(serviceClass: KClass<T>): T {
        return serviceCache.getOrPut(serviceClass) {
            transparentClient.withService(serviceClass)
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    open fun <@Rpc T : Any> getService(serviceClass: KClass<T>, client: RpcClient): T {
        return client.withService(serviceClass)
    }

    inline fun <@Rpc reified T : Any> getService(): T {
        return getService(T::class)
    }

    inline fun <@Rpc reified T : Any> getService(client: RpcClient): T {
        return getService(T::class, client)
    }

    suspend fun clear() {
        mutex.withLock {
            transparentClient.close()
            val oldClient = _servicesClient
            _servicesClient = null
            try {
                oldClient?.close()
            } catch (_: Throwable) {
            }
            serviceCache.clear()
        }
    }
}

private class SessionExpiredException :
    IllegalStateException("Session expired and no refresh token available")
