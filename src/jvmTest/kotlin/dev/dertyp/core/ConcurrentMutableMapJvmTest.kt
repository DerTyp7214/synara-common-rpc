package dev.dertyp.core

import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentMutableMapJvmTest {

    @Test
    fun testConcurrency() = runBlocking {
        val map = concurrentMutableMapOf<Int, Int>()
        val jobs = mutableListOf<Job>()
        val count = 1000
        val iterations = 100

        repeat(count) { i ->
            jobs += launch(Dispatchers.Default) {
                repeat(iterations) { j ->
                    map[i] = j
                }
            }
        }

        jobs.joinAll()
        assertEquals(count, map.size)
        
        map.clear()
        
        val jobs2 = mutableListOf<Job>()
        repeat(count) { i ->
            jobs2 += launch(Dispatchers.Default) {
                repeat(iterations) { j ->
                    map.getOrPut(i) { j }
                }
            }
        }
        
        jobs2.joinAll()
        assertEquals(count, map.size)
    }
}
