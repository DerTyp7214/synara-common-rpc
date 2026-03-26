package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ListsTest {

    @Test
    fun testMinusOnce() {
        val list = listOf(1, 2, 2, 3)
        val other = listOf(2)
        assertEquals(listOf(1, 2, 3), list.minusOnce(other))
    }

    @Test
    fun testSplice() {
        val list = mutableListOf(1, 2, 3, 4, 5)
        
        // Remove 2 elements from index 1 and add 10, 11
        val removed = list.splice(1, 2, 10, 11)
        
        assertEquals(listOf(2, 3), removed)
        assertEquals(listOf(1, 10, 11, 4, 5), list)
    }

    @Test
    fun testSpliceNegativeStart() {
        val list = mutableListOf(1, 2, 3, 4, 5)
        
        // Negative start index (-2 refers to index 3)
        val removed = list.splice(-2, 1, 99)
        
        assertEquals(listOf(4), removed)
        assertEquals(listOf(1, 2, 3, 99, 5), list)
    }

    @Test
    fun testRemoveFirst() {
        val list = mutableListOf(1, 2, 3, 2, 4)
        val removed = list.removeFirst { it == 2 }
        
        assertEquals(2, removed)
        assertEquals(listOf(1, 3, 2, 4), list)
    }
}
