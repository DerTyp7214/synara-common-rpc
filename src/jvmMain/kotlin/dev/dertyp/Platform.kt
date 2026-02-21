package dev.dertyp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

actual typealias PlatformDate = Date
actual typealias PlatformLocalDate = LocalDate
actual typealias PlatformLocalDateTime = LocalDateTime
actual typealias PlatformOffsetDateTime = OffsetDateTime
actual typealias PlatformInstant = Instant

actual typealias PlatformUUID = UUID

actual fun randomPlatformUUID(): PlatformUUID = UUID.randomUUID()
actual fun platformUUIDFromString(string: String): PlatformUUID = UUID.fromString(string)

actual fun PlatformUUID.toByteArray(): ByteArray {
    val bb = ByteBuffer.allocate(16)
    bb.putLong(this.mostSignificantBits)
    bb.putLong(this.leastSignificantBits)
    return bb.array()
}

actual fun String.toPlatformUUID(): PlatformUUID = UUID.fromString(this)

actual fun ByteArray.toPlatformUUID(): PlatformUUID {
    val bb = ByteBuffer.wrap(this)
    val firstLong = bb.long
    val secondLong = bb.long
    return UUID(firstLong, secondLong)
}

actual fun PlatformOffsetDateTime.formatISO(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
actual fun String.toPlatformOffsetDateTimeISO(): PlatformOffsetDateTime = OffsetDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

actual fun PlatformLocalDate.formatISO(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
actual fun String.toPlatformLocalDateISO(): PlatformLocalDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)

actual fun PlatformLocalDateTime.formatISO(): String = format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
actual fun String.toPlatformLocalDateTimeISO(): PlatformLocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

actual fun PlatformDate.toEpochMilliseconds(): Long = this.time
actual fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate = Date(ms)
actual fun PlatformDate.formatISO(): String = toInstant().toString()
actual fun String.toPlatformDateISO(): PlatformDate = Date.from(java.time.Instant.parse(this))

actual fun PlatformInstant.toEpochMilliseconds(): Long = this.toEpochMilli()
actual fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant = Instant.ofEpochMilli(ms)
actual fun PlatformInstant.formatISO(): String = toString()
actual fun String.toPlatformInstantISO(): PlatformInstant = Instant.parse(this)

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun nowAsPlatformDate(): PlatformDate = Date()
actual fun nowAsPlatformInstant(): PlatformInstant = Instant.now()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
