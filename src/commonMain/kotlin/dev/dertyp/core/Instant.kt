package dev.dertyp.core

import dev.dertyp.PlatformInstant
import dev.dertyp.platformInstantFromEpochMilliseconds
import dev.dertyp.toEpochMilliseconds
import kotlin.time.Duration

operator fun PlatformInstant.plus(duration: Duration): PlatformInstant {
    return platformInstantFromEpochMilliseconds(toEpochMilliseconds() + duration.inWholeMilliseconds)
}