package dev.dertyp.core

import dev.dertyp.platformDateFromEpochMilliseconds
import dev.dertyp.toEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class DateTest {

    @Test
    fun testDatePlusDuration() {
        val initial = platformDateFromEpochMilliseconds(1000000)
        val duration = 1.hours
        val expected = platformDateFromEpochMilliseconds(1000000 + duration.inWholeMilliseconds)
        
        val result = initial + duration
        assertEquals(expected.toEpochMilliseconds(), result.toEpochMilliseconds())
    }
}
