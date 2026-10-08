package dev.dertyp.rpc

import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.rpc.RpcCall
import kotlinx.rpc.RpcClient
import kotlinx.rpc.krpc.ktor.client.KtorRpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlin.time.Duration.Companion.milliseconds

fun HttpClient.reconnectingRpcClient(
    onFailure: () -> Unit = {},
    maxRetries: Int = 5,
    delayMs: Long = 1000L,
    block: HttpRequestBuilder.() -> Unit
): RpcClient {
    return ReconnectingRpcClient(
        delegateProvider = { rpc(block) },
        onFailure = onFailure,
        maxRetries = maxRetries,
        delayMs = delayMs,
        ownsDelegate = true
    )
}

class ReconnectingRpcClient(
    private val delegateProvider: suspend () -> KtorRpcClient,
    private val onCancel: suspend (KtorRpcClient) -> Unit = {},
    private val onFailure: () -> Unit = {},
    private val maxRetries: Int = 5,
    private val delayMs: Long = 1000L,
    private val ownsDelegate: Boolean = false
) : RpcClient {

    private fun release(delegate: KtorRpcClient) {
        if (ownsDelegate) delegate.close()
    }

    override suspend fun <T> call(call: RpcCall): T {
        var attempts = 0
        while (true) {
            val delegate = delegateProvider()
            return try {
                delegate.call<T>(call).also { release(delegate) }
            } catch (e: Throwable) {
                release(delegate)
                if (e is CancellationException) throw e
                attempts++
                if (isRetriable(e) && attempts < maxRetries) {
                    onCancel(delegate)
                    delay((delayMs * attempts).milliseconds)
                    continue
                }
                if (e.isTransportFailure()) onFailure()
                throw e
            }
        }
    }

    override fun <T> callServerStreaming(call: RpcCall): Flow<T> = flow {
        var failed: KtorRpcClient? = null
        val attempts = flow {
            val delegate = delegateProvider()
            failed = delegate
            try {
                emitAll(delegate.callServerStreaming<T>(call))
            } finally {
                release(delegate)
            }
        }.retryWhen { e, attempt ->
            if (isRetriable(e) && attempt < maxRetries) {
                failed?.let { onCancel(it) }
                delay((delayMs * 2).milliseconds)
                true
            } else {
                if (e.isTransportFailure()) onFailure()
                false
            }
        }
        emitAll(attempts)
    }

    private fun isRetriable(e: Throwable): Boolean {
        if (e is UnresolvedAddressException) return false
        return e.isTransportFailure()
    }
}

class RpcClientPool(
    private val size: Int,
    private val connect: suspend () -> KtorRpcClient
) {
    private val mutex = Mutex()
    private val slots = arrayOfNulls<KtorRpcClient>(size)
    private var next = 0
    private var generation = 0

    suspend fun acquire(): KtorRpcClient {
        while (true) {
            val (index, seen) = mutex.withLock {
                val index = next
                next = (next + 1) % size
                slots[index]?.let { return it }
                index to generation
            }
            val fresh = connect()
            val stored = mutex.withLock {
                when {
                    generation != seen -> null
                    else -> slots[index] ?: fresh.also { slots[index] = it }
                }
            }
            if (stored === fresh) return fresh
            runCatching { fresh.close() }
            if (stored != null) return stored
        }
    }

    suspend fun invalidate(stale: KtorRpcClient) {
        mutex.withLock {
            val index = slots.indexOfFirst { it === stale }
            if (index >= 0) slots[index] = null
        }
        runCatching { stale.close() }
    }

    suspend fun close() {
        val open = mutex.withLock {
            generation++
            slots.filterNotNull().also { slots.fill(null) }
        }
        open.forEach { runCatching { it.close() } }
    }
}

fun Throwable.isTransportFailure(): Boolean = when (this) {
    is ClosedSendChannelException,
    is ClosedReceiveChannelException,
    is ConnectTimeoutException,
    is HttpRequestTimeoutException,
    is UnresolvedAddressException,
    is IOException -> true

    is WebSocketException -> message?.contains("401") != true
    is IllegalStateException -> message?.contains("RpcClient was cancelled") == true
    else -> false
}
