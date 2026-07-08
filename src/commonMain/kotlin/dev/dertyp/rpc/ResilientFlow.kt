package dev.dertyp.rpc

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Wraps an infinite observation stream so it survives connection loss: on error or
 * completion the flow waits for [gate] to emit true, then resubscribes via [factory].
 *
 * Only meant for observation streams that should never terminate; finite data streams
 * must keep their fail/complete semantics.
 */
fun <T> resilientObservation(
    gate: Flow<Boolean>,
    isFatal: (Throwable) -> Boolean = { false },
    initialBackoff: Duration = 1.seconds,
    maxBackoff: Duration = 30.seconds,
    onError: (Throwable) -> Unit = {},
    factory: () -> Flow<T>,
): Flow<T> = flow {
    var backoff = initialBackoff
    while (true) {
        try {
            factory().collect { value ->
                backoff = initialBackoff
                try {
                    emit(value)
                } catch (e: Throwable) {
                    // Exceptions from the downstream collector (aborts from take/first,
                    // collector failures) must not be treated as upstream errors.
                    throw DownstreamException(e)
                }
            }
        } catch (e: DownstreamException) {
            throw e.cause
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            if (isFatal(e)) throw e
            onError(e)
        }
        gate.first { it }
        delay(backoff)
        backoff = (backoff * 2).coerceAtMost(maxBackoff)
    }
}

private class DownstreamException(override val cause: Throwable) : Exception(cause)
