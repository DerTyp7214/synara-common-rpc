package dev.dertyp.rpc

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ResilientFlowTest {

    @Test
    fun resubscribesAfterErrorOnceGateOpens() = runTest {
        val gate = MutableStateFlow(false)
        var attempts = 0
        val collected = mutableListOf<Int>()

        val job = launch {
            resilientObservation(gate = gate) {
                attempts++
                if (attempts == 1) flow { emit(1); throw RuntimeException("boom") }
                else flowOf(2, 3)
            }.take(3).toList(collected)
        }

        runCurrent()
        assertEquals(listOf(1), collected)
        assertEquals(1, attempts)

        advanceTimeBy(60.seconds)
        runCurrent()
        assertEquals(1, attempts)

        gate.value = true
        advanceTimeBy(2.seconds)
        runCurrent()
        assertEquals(2, attempts)
        assertEquals(listOf(1, 2, 3), collected)
        job.join()
    }

    @Test
    fun resubscribesAfterNormalCompletion() = runTest {
        val gate = MutableStateFlow(true)
        var attempts = 0
        val collected = mutableListOf<Int>()

        val job = launch {
            resilientObservation(gate = gate) {
                attempts++
                flowOf(attempts)
            }.take(3).toList(collected)
        }

        advanceTimeBy(120.seconds)
        runCurrent()
        assertEquals(listOf(1, 2, 3), collected)
        job.join()
    }

    @Test
    fun stopsOnFatalError() = runTest {
        val gate = MutableStateFlow(true)
        var attempts = 0
        var caught: Throwable? = null

        val job = launch {
            try {
                resilientObservation(
                    gate = gate,
                    isFatal = { it.message == "fatal" },
                ) {
                    attempts++
                    flow<Int> { throw IllegalStateException("fatal") }
                }.toList()
            } catch (e: IllegalStateException) {
                caught = e
            }
        }

        advanceTimeBy(60.seconds)
        runCurrent()
        assertEquals(1, attempts)
        assertEquals("fatal", caught?.message)
        job.join()
    }

    @Test
    fun rethrowsOnCollectorCancellation() = runTest {
        val gate = MutableStateFlow(true)
        var attempts = 0

        val job = launch {
            resilientObservation(gate = gate) {
                attempts++
                flow { emit(1); kotlinx.coroutines.awaitCancellation() }
            }.toList()
        }

        runCurrent()
        assertEquals(1, attempts)
        job.cancelAndJoin()

        advanceTimeBy(60.seconds)
        runCurrent()
        assertEquals(1, attempts)
    }

    @Test
    fun backoffGrowsAndResetsOnEmission() = runTest {
        val gate = MutableStateFlow(true)
        var attempts = 0
        val collected = mutableListOf<Int>()

        val job = launch {
            resilientObservation(
                gate = gate,
                initialBackoff = 1.seconds,
                maxBackoff = 8.seconds,
            ) {
                attempts++
                when (attempts) {
                    1, 2 -> flow { throw RuntimeException("down") }
                    3 -> flowOf(42)
                    else -> flow { throw RuntimeException("down again") }
                }
            }.take(1).toList(collected)
        }

        runCurrent()
        assertEquals(1, attempts)
        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(2, attempts)
        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(2, attempts)
        advanceTimeBy(1.seconds)
        runCurrent()
        assertTrue(attempts >= 3)
        assertEquals(listOf(42), collected)
        job.join()
    }
}
