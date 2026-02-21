package dev.dertyp

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

actual typealias PlatformUUID = Uuid

actual fun randomPlatformUUID(): PlatformUUID = Uuid.random()
actual fun platformUUIDFromString(string: String): PlatformUUID = Uuid.parse(string)
actual fun PlatformUUID.toByteArray(): ByteArray = this.toByteArray()
actual fun String.toPlatformUUID(): PlatformUUID = Uuid.parse(this)
actual fun ByteArray.toPlatformUUID(): PlatformUUID = Uuid.fromByteArray(this)

// For Windows/Native, we use Instant and simple wrappers
actual class PlatformDate(val epochMillis: Long)
actual fun PlatformDate.toEpochMilliseconds(): Long = epochMillis
actual fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate = PlatformDate(ms)
actual fun PlatformDate.formatISO(): String = Instant.fromEpochMilliseconds(epochMillis).toString()
actual fun String.toPlatformDateISO(): PlatformDate = PlatformDate(Instant.parse(this).toEpochMilliseconds())

actual class PlatformInstant(val epochMillis: Long)
actual fun PlatformInstant.toEpochMilliseconds(): Long = epochMillis
actual fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant = PlatformInstant(ms)
actual fun PlatformInstant.formatISO(): String = Instant.fromEpochMilliseconds(epochMillis).toString()
actual fun String.toPlatformInstantISO(): PlatformInstant = PlatformInstant(Instant.parse(this).toEpochMilliseconds())

actual class PlatformLocalDate(val isoString: String)
actual fun PlatformLocalDate.formatISO(): String = isoString
actual fun String.toPlatformLocalDateISO(): PlatformLocalDate = PlatformLocalDate(this)

actual class PlatformLocalDateTime(val isoString: String)
actual fun PlatformLocalDateTime.formatISO(): String = isoString
actual fun String.toPlatformLocalDateTimeISO(): PlatformLocalDateTime = PlatformLocalDateTime(this)

actual class PlatformOffsetDateTime(val isoString: String)
actual fun PlatformOffsetDateTime.formatISO(): String = isoString
actual fun String.toPlatformOffsetDateTimeISO(): PlatformOffsetDateTime = PlatformOffsetDateTime(this)

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
actual fun nowAsPlatformDate(): PlatformDate = PlatformDate(currentTimeMillis())
actual fun nowAsPlatformInstant(): PlatformInstant = PlatformInstant(currentTimeMillis())

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
