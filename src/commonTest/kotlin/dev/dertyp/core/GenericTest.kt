package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class GenericTest {

    @Test
    fun testIfNull() {
        val name: String? = null
        assertEquals("Default", name.ifNull { "Default" })
        
        val validName = "Synara"
        assertEquals("Synara", validName.ifNull { "Default" })
    }
}
