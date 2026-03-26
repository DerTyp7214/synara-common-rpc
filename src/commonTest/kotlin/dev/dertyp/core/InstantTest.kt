package dev.dertyp.core

import dev.dertyp.platformInstantFromEpochMilliseconds
import dev.dertyp.toEpochMilliseconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class InstantTest {

    @Test
    fun testInstantPlusDuration() {
        val initial = platformInstantFromEpochMilliseconds(1000)
        val duration = 5.minutes
        val expected = platformInstantFromEpochMilliseconds(1000 + duration.inWholeMilliseconds)
        
        val result = initial + duration
        assertEquals(expected.toEpochMilliseconds(), result.toEpochMilliseconds())
    }
}
