@file:OptIn(ExperimentalForeignApi::class)

package dev.dertyp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*

actual typealias PlatformUUID = NSUUID

actual fun randomPlatformUUID(): PlatformUUID = NSUUID()
actual fun platformUUIDFromString(string: String): PlatformUUID = NSUUID(uUIDString = string)

actual fun PlatformUUID.toByteArray(): ByteArray {
    val bytes = ByteArray(16)
    bytes.usePinned { pinned ->
        this.getUUIDBytes(pinned.addressOf(0).reinterpret())
    }
    return bytes
}

actual fun String.toPlatformUUID(): PlatformUUID = NSUUID(uUIDString = this)

actual fun ByteArray.toPlatformUUID(): PlatformUUID {
    return this.usePinned { pinned ->
        NSUUID(uUIDBytes = pinned.addressOf(0).reinterpret())
    }
}

private val isoDateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    locale = NSLocale.currentLocale
    timeZone = NSTimeZone.localTimeZone
}

actual class PlatformDate(val value: NSDate)
actual fun PlatformDate.toEpochMilliseconds(): Long = (value.timeIntervalSince1970 * 1000).toLong()
actual fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate = PlatformDate(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))
actual fun PlatformDate.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformDateISO(): PlatformDate = PlatformDate(isoDateFormatter.dateFromString(this) ?: NSDate())

actual class PlatformInstant(val value: NSDate)
actual fun PlatformInstant.toEpochMilliseconds(): Long = (value.timeIntervalSince1970 * 1000).toLong()
actual fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant = PlatformInstant(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))
actual fun PlatformInstant.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformInstantISO(): PlatformInstant = PlatformInstant(isoDateFormatter.dateFromString(this) ?: NSDate())

actual class PlatformLocalDate(val value: NSDate)
actual fun PlatformLocalDate.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformLocalDateISO(): PlatformLocalDate = PlatformLocalDate(isoDateFormatter.dateFromString(this) ?: NSDate())

actual class PlatformLocalDateTime(val value: NSDate)
actual fun PlatformLocalDateTime.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformLocalDateTimeISO(): PlatformLocalDateTime = PlatformLocalDateTime(isoDateFormatter.dateFromString(this) ?: NSDate())

actual class PlatformOffsetDateTime(val value: NSDate)
actual fun PlatformOffsetDateTime.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformOffsetDateTimeISO(): PlatformOffsetDateTime = PlatformOffsetDateTime(isoDateFormatter.dateFromString(this) ?: NSDate())

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
actual fun nowAsPlatformDate(): PlatformDate = PlatformDate(NSDate())
actual fun nowAsPlatformInstant(): PlatformInstant = PlatformInstant(NSDate())

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
