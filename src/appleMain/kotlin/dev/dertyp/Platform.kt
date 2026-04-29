@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class, ExperimentalUuidApi::class)
@file:Suppress("unused")

package dev.dertyp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual typealias PlatformUUID = Uuid

actual fun randomPlatformUUID(): PlatformUUID = Uuid.random()
actual fun platformUUIDFromString(string: String): PlatformUUID = Uuid.parse(string)

actual fun PlatformUUID.toByteArray(): ByteArray = this.toByteArray()

actual fun String.toPlatformUUID(): PlatformUUID = Uuid.parse(this)

actual fun ByteArray.toPlatformUUID(): PlatformUUID = Uuid.fromByteArray(this)

private val isoDateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    timeZone = NSTimeZone.localTimeZone
}

private val localDateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd"
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    timeZone = NSTimeZone.localTimeZone
}

private val yearDateFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy"
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    timeZone = NSTimeZone.localTimeZone
}

private val dateTimeFormatter = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd HH:mm:ss"
    locale = NSLocale.currentLocale
    timeZone = NSTimeZone.localTimeZone
}

private val dateFormatter = NSDateFormatter().apply {
    dateFormat = "d. MMM yyyy"
    locale = NSLocale.currentLocale
    timeZone = NSTimeZone.localTimeZone
}

actual class PlatformDate(val value: NSDate)
actual fun PlatformDate.toEpochMilliseconds(): Long = (value.timeIntervalSince1970 * 1000).toLong()
actual fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate = PlatformDate(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))
actual fun PlatformDate.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformDateISO(): PlatformDate = PlatformDate(isoDateFormatter.dateFromString(this) ?: NSDate())
actual fun PlatformDate.formatDate(): String = dateFormatter.stringFromDate(value)

actual class PlatformInstant(val value: NSDate)
actual fun PlatformInstant.toEpochMilliseconds(): Long = (value.timeIntervalSince1970 * 1000).toLong()
actual fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant = PlatformInstant(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))
actual fun PlatformInstant.formatISO(): String = isoDateFormatter.stringFromDate(value)
actual fun String.toPlatformInstantISO(): PlatformInstant = PlatformInstant(isoDateFormatter.dateFromString(this) ?: NSDate())
actual fun PlatformInstant.formatDateTime(): String = dateTimeFormatter.stringFromDate(value)

actual class PlatformLocalDate(val value: NSDate)
actual fun PlatformLocalDate.formatISO(): String = localDateFormatter.stringFromDate(value)
actual fun String.toPlatformLocalDateISO(): PlatformLocalDate {
    val date = localDateFormatter.dateFromString(this)
        ?: isoDateFormatter.dateFromString(this)
        ?: yearDateFormatter.dateFromString(this)
        ?: NSDate()
    return PlatformLocalDate(date)
}

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

actual fun getStacktrace(): String? = NSThread.callStackSymbols.joinToString("\n")

actual fun getPlatformName(): String {
    return when (Platform.osFamily) {
        OsFamily.IOS -> "iPhone"
        OsFamily.MACOSX -> "Macintosh"
        OsFamily.TVOS -> "tvOS"
        OsFamily.WATCHOS -> "watchOS"
        else -> "Apple Device"
    }
}
