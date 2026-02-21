package dev.dertyp.rpc

import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.io.IOException
import kotlinx.rpc.RpcCall
import kotlinx.rpc.RpcClient
import kotlinx.rpc.krpc.ktor.client.rpc

fun HttpClient.reconnectingRpcClient(
    onCancel: () -> Unit = {},
    onFailure: () -> Unit = {},
    maxRetries: Int = 5,
    delayMs: Long = 1000L,
    block: HttpRequestBuilder.() -> Unit
): RpcClient {
    return ReconnectingRpcClient(
        delegateProvider = { rpc(block) },
        onCancel = onCancel,
        onFailure = onFailure,
        maxRetries = maxRetries,
        delayMs = delayMs
    )
}

class ReconnectingRpcClient(
    private val delegateProvider: suspend () -> RpcClient,
    private val onCancel: () -> Unit = {},
    private val onFailure: () -> Unit = {},
    private val maxRetries: Int = 5,
    private val delayMs: Long = 1000L
) : RpcClient {
    override suspend fun <T> call(call: RpcCall): T {
        var attempts = 0
        while (true) {
            return try {
                delegateProvider().call<T>(call)
            } catch (e: Throwable) {
                attempts++
                if (isRetriable(e) && attempts < maxRetries) {
                    onCancel()
                    delay(delayMs * attempts)
                    continue
                }
                onFailure()
                throw e
            }
        }
    }

    override fun <T> callServerStreaming(call: RpcCall): Flow<T> {
        return flow {
            emitAll(delegateProvider().callServerStreaming<T>(call))
        }.retry(retries = maxRetries.toLong()) { e ->
            if (isRetriable(e)) {
                onCancel()
                delay(delayMs * 2)
                true
            } else {
                onFailure()
                false
            }
        }
    }

    private fun isRetriable(e: Throwable): Boolean {
        if (e is UnresolvedAddressException) return false

        val isCancelled = e is IllegalStateException && e.message?.contains("RpcClient was cancelled") == true
        
        return isCancelled ||
                e is ConnectTimeoutException ||
                e is HttpRequestTimeoutException ||
                e is IOException
    }
}
