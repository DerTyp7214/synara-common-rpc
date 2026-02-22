package dev.dertyp.core

import dev.dertyp.PlatformDate
import dev.dertyp.platformDateFromEpochMilliseconds
import dev.dertyp.toEpochMilliseconds
import kotlin.time.Duration

operator fun PlatformDate.plus(duration: Duration): PlatformDate = platformDateFromEpochMilliseconds(toEpochMilliseconds() + duration.inWholeMilliseconds)