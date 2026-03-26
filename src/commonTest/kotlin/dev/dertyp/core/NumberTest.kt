package dev.dertyp.core

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberTest {

    @Test
    fun testIfZero() {
        assertEquals(5, 0.ifZero(5))
        assertEquals(3, 3.ifZero(5))
    }

    @Test
    fun testRoundToNDecimals() {
        assertEquals(1.2f, 1.234f.roundToNDecimals(1))
        assertEquals(1.23, 1.234.roundToNDecimals(2))
    }

    @Test
    fun testDigitCount() {
        assertEquals(1, 0.digitCount())
        assertEquals(1, 5.digitCount())
        assertEquals(2, 10.digitCount())
        assertEquals(3, 123.digitCount())
        assertEquals(3, (-123).digitCount())
    }

    @Test
    fun testZeroPad() {
        assertEquals("05", 5.zeroPad(2))
        assertEquals("123", 123.zeroPad(2))
        assertEquals("00123", 123.zeroPad(5))
    }
}
