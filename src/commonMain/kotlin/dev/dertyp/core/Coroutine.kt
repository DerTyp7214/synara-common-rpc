@file:JvmName("CommonCoroutine")
@file:Suppress("unused")

package dev.dertyp.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlin.jvm.JvmName

suspend inline infix fun <T> Deferred<T>.catch(crossinline action: (Throwable) -> T): T {
    return try {
        await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        action(e)
    }
}

suspend inline infix fun <T> Deferred<T>.ifCatch(defaultValue: T): T {
    return try {
        await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        defaultValue
    }
}

suspend inline fun <R> (suspend () -> R).catch(
    crossinline action: (Throwable) -> R
): R = try { this() } catch (e: CancellationException) { throw e } catch (e: Throwable) { action(e) }

suspend inline infix fun <R> (suspend () -> R).ifCatch(
    defaultValue: R
): R = try { this() } catch (e: CancellationException) { throw e } catch (e: Throwable) { defaultValue }

suspend inline fun <P1, R> (suspend (P1) -> R).catch(
    p1: P1,
    crossinline action: (Throwable) -> R
): R = try { this(p1) } catch (e: CancellationException) { throw e } catch (e: Throwable) { action(e) }

suspend inline fun <P1, R> (suspend (P1) -> R).ifCatch(
    p1: P1,
    defaultValue: R
): R = try { this(p1) } catch (e: CancellationException) { throw e } catch (e: Throwable) { defaultValue }

suspend inline fun <P1, P2, R> (suspend (P1, P2) -> R).catch(
    p1: P1, p2: P2,
    crossinline action: (Throwable) -> R
): R = try { this(p1, p2) } catch (e: CancellationException) { throw e } catch (e: Throwable) { action(e) }

suspend inline fun <P1, P2, R> (suspend (P1, P2) -> R).ifCatch(
    p1: P1, p2: P2,
    defaultValue: R
): R = try { this(p1, p2) } catch (e: CancellationException) { throw e } catch (e: Throwable) { defaultValue }

suspend inline fun <P1, P2, P3, R> (suspend (P1, P2, P3) -> R).catch(
    p1: P1, p2: P2, p3: P3,
    crossinline action: (Throwable) -> R
): R = try { this(p1, p2, p3) } catch (e: CancellationException) { throw e } catch (e: Throwable) { action(e) }

suspend inline fun <P1, P2, P3, R> (suspend (P1, P2, P3) -> R).ifCatch(
    p1: P1, p2: P2, p3: P3,
    defaultValue: R
): R = try { this(p1, p2, p3) } catch (e: CancellationException) { throw e } catch (e: Throwable) { defaultValue }
