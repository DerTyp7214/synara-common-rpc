package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConcurrentMutableMapTest {

    @Test
    fun testBasicOperations() {
        val map = concurrentMutableMapOf<String, Int>()
        
        assertTrue(map.isEmpty())
        assertEquals(0, map.size)
        
        map["one"] = 1
        assertEquals(1, map["one"])
        assertEquals(1, map.size)
        assertFalse(map.isEmpty())
        
        assertTrue(map.containsKey("one"))
        assertTrue(map.containsValue(1))
        
        map.putAll(mapOf("two" to 2, "three" to 3))
        assertEquals(3, map.size)
        
        assertEquals(1, map.remove("one"))
        assertEquals(2, map.size)
        
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun testGetOrPut() {
        val map = concurrentMutableMapOf<String, Int>()
        
        val value = map.getOrPut("key") { 42 }
        assertEquals(42, value)
        assertEquals(42, map["key"])
        
        val existing = map.getOrPut("key") { 100 }
        assertEquals(42, existing)
        assertEquals(42, map["key"])
    }

    @Test
    fun testViews() {
        val map = concurrentMutableMapOf<String, Int>()
        map["a"] = 1
        map["b"] = 2
        
        assertEquals(setOf("a", "b"), map.keys)
        assertEquals(setOf(1, 2), map.values.toSet())
        assertEquals(2, map.entries.size)
    }
}
