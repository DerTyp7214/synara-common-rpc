package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CollectionsTest {

    @Test
    fun testTakeEvery() {
        val list = listOf(1, 2, 3, 4, 5, 6)
        assertEquals(listOf(1, 3, 5), list.takeEvery(2))
        assertEquals(listOf(1, 4), list.takeEvery(3))
        assertEquals(listOf(1), list.takeEvery(10))
    }

    @Test
    fun testNullIfEmpty() {
        assertEquals(listOf(1), listOf(1).nullIfEmpty())
        assertNull(emptyList<Int>().nullIfEmpty())
    }

    @Test
    fun testDivAssign() {
        val list = mutableListOf(1, 2, 3)
        list /= listOf(4, 5)
        assertEquals(listOf(4, 5), list)
    }

    @Test
    fun testReplaceWith() {
        val list = mutableListOf(1, 2, 3)
        list replaceWith listOf(10, 20)
        assertEquals(listOf(10, 20), list)
    }
}
