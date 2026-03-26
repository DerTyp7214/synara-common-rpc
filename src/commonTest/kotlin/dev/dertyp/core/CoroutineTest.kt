package dev.dertyp.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTest {

    @Test
    fun testDeferredCatchSuccess() = runTest {
        val deferred = CompletableDeferred(10)
        val result = deferred.catch { 0 }
        assertEquals(10, result)
    }

    @Test
    fun testDeferredCatchFailure() = runTest {
        val deferred = CompletableDeferred<Int>()
        deferred.completeExceptionally(RuntimeException("Error"))
        val result = deferred.catch { 0 }
        assertEquals(0, result)
    }

    @Test
    fun testSuspendLambdaCatchSuccess() = runTest {
        val action: suspend () -> Int = { 42 }
        assertEquals(42, action.catch { 0 })
    }

    @Test
    fun testSuspendLambdaCatchFailure() = runTest {
        val action: suspend () -> Int = { throw RuntimeException("Fail") }
        assertEquals(0, action.catch { 0 })
    }

    @Test
    fun testSuspendLambdaIfCatchSuccess() = runTest {
        val action: suspend (Int) -> Int = { it * 2 }
        assertEquals(20, action.ifCatch(10, 0))
    }

    @Test
    fun testSuspendLambdaIfCatchFailure() = runTest {
        val action: suspend (Int) -> Int = { throw RuntimeException("Fail") }
        assertEquals(0, action.ifCatch(10, 0))
    }
}
