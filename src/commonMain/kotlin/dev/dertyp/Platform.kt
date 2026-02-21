@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package dev.dertyp

expect class PlatformUUID

expect fun randomPlatformUUID(): PlatformUUID
expect fun platformUUIDFromString(string: String): PlatformUUID

expect fun PlatformUUID.toByteArray(): ByteArray
expect fun String.toPlatformUUID(): PlatformUUID
expect fun ByteArray.toPlatformUUID(): PlatformUUID

expect class PlatformOffsetDateTime

expect fun PlatformOffsetDateTime.formatISO(): String
expect fun String.toPlatformOffsetDateTimeISO(): PlatformOffsetDateTime

expect class PlatformLocalDate

expect fun PlatformLocalDate.formatISO(): String
expect fun String.toPlatformLocalDateISO(): PlatformLocalDate

expect class PlatformLocalDateTime

expect fun PlatformLocalDateTime.formatISO(): String
expect fun String.toPlatformLocalDateTimeISO(): PlatformLocalDateTime

expect class PlatformDate

expect fun PlatformDate.toEpochMilliseconds(): Long
expect fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate
expect fun PlatformDate.formatISO(): String
expect fun String.toPlatformDateISO(): PlatformDate

expect class PlatformInstant

expect fun PlatformInstant.toEpochMilliseconds(): Long
expect fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant
expect fun PlatformInstant.formatISO(): String
expect fun String.toPlatformInstantISO(): PlatformInstant

expect fun currentTimeMillis(): Long
expect fun nowAsPlatformDate(): PlatformDate
expect fun nowAsPlatformInstant(): PlatformInstant
