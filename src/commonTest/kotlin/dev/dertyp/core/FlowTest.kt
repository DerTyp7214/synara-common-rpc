package dev.dertyp.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FlowTest {

    @Test
    fun testFlowPlus() = runTest {
        val flow1 = flowOf(1, 2)
        val flow2 = flowOf(3, 4)
        val combined = flow1 + flow2
        assertEquals(listOf(1, 2, 3, 4), combined.toList())
    }

    @Test
    fun testCombine6() = runTest {
        val f1 = flowOf(1)
        val f2 = flowOf(2)
        val f3 = flowOf(3)
        val f4 = flowOf(4)
        val f5 = flowOf(5)
        val f6 = flowOf(6)
        
        val combined = combine(f1, f2, f3, f4, f5, f6) { v1, v2, v3, v4, v5, v6 ->
            v1 + v2 + v3 + v4 + v5 + v6
        }
        
        assertEquals(21, combined.first())
    }
}
