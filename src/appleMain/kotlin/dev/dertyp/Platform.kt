@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalNativeApi::class,
    ExperimentalUuidApi::class,
    DelicateCoroutinesApi::class
)
@file:Suppress("unused")

package dev.dertyp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

private val posix = NSLocale(localeIdentifier = "en_US_POSIX")
private val utc = NSTimeZone.timeZoneForSecondsFromGMT(0)

private fun utcFormatter(format: String) = NSDateFormatter().apply {
    dateFormat = format
    locale = posix
    timeZone = utc
}

private fun isoFormatter(options: ULong) = NSISO8601DateFormatter().apply {
    formatOptions = options
    timeZone = utc
}

private val isoInstantFormatter = isoFormatter(NSISO8601DateFormatWithInternetDateTime or NSISO8601DateFormatWithFractionalSeconds)
private val isoInstantParsers = listOf(
    isoInstantFormatter,
    isoFormatter(NSISO8601DateFormatWithInternetDateTime),
)

private val localDateFormatter = utcFormatter("yyyy-MM-dd")
private val yearDateFormatter = utcFormatter("yyyy")
private val localDateTimeFormatter = utcFormatter("yyyy-MM-dd'T'HH:mm:ss.SSS")
private val localDateTimeParsers = listOf(
    utcFormatter("yyyy-MM-dd'T'HH:mm:ss"),
    utcFormatter("yyyy-MM-dd'T'HH:mm"),
)

private fun NSDate.formatInstant(): String = isoInstantFormatter.stringFromDate(this).replace(".000Z", "Z")

private val subMillisFraction = Regex("""(\.\d{3})\d+""")

private fun String.parseInstant(): NSDate {
    val text = replace(subMillisFraction, "$1")
    return isoInstantParsers.firstNotNullOfOrNull { it.dateFromString(text) }
        ?: throw IllegalArgumentException("Text '$this' could not be parsed as an ISO-8601 instant")
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
actual fun platformDateFromEpochMilliseconds(ms: Long): PlatformDate =
    PlatformDate(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))

actual fun PlatformDate.formatISO(): String = value.formatInstant()
actual fun String.toPlatformDateISO(): PlatformDate = PlatformDate(parseInstant())
actual fun PlatformDate.formatDate(): String = dateFormatter.stringFromDate(value)

actual class PlatformInstant(val value: NSDate)

actual fun PlatformInstant.toEpochMilliseconds(): Long = (value.timeIntervalSince1970 * 1000).toLong()
actual fun platformInstantFromEpochMilliseconds(ms: Long): PlatformInstant =
    PlatformInstant(NSDate.dateWithTimeIntervalSince1970(ms / 1000.0))

actual fun PlatformInstant.formatISO(): String = value.formatInstant()
actual fun String.toPlatformInstantISO(): PlatformInstant = PlatformInstant(parseInstant())

actual fun PlatformInstant.formatDateTime(): String = dateTimeFormatter.stringFromDate(value)

actual class PlatformLocalDate(val value: NSDate)

actual fun PlatformLocalDate.formatISO(): String = localDateFormatter.stringFromDate(value)
actual fun String.toPlatformLocalDateISO(): PlatformLocalDate {
    val text = if (length > 10 && this[10] == 'T') substring(0, 10) else this
    val date = localDateFormatter.dateFromString(text)
        ?: yearDateFormatter.dateFromString(text)
        ?: throw IllegalArgumentException("Text '$this' could not be parsed as a local date")
    return PlatformLocalDate(date)
}

actual class PlatformLocalDateTime(val value: NSDate)

actual fun PlatformLocalDateTime.formatISO(): String = localDateTimeFormatter.stringFromDate(value).removeSuffix(".000")
actual fun String.toPlatformLocalDateTimeISO(): PlatformLocalDateTime {
    val base = substringBefore('.')
    val digits = substringAfter('.', "")
    val date = if (digits.isNotEmpty() && !digits.all { it in '0'..'9' }) null
    else localDateTimeParsers.firstNotNullOfOrNull { it.dateFromString(base) }
    if (date == null || (contains('.') && digits.isEmpty()))
        throw IllegalArgumentException("Text '$this' could not be parsed as a local date-time")
    return PlatformLocalDateTime(date.dateByAddingTimeInterval(if (digits.isEmpty()) 0.0 else "0.$digits".toDouble()))
}

actual class PlatformOffsetDateTime(val value: NSDate)

actual fun PlatformOffsetDateTime.formatISO(): String = value.formatInstant()
actual fun String.toPlatformOffsetDateTimeISO(): PlatformOffsetDateTime = PlatformOffsetDateTime(parseInstant())

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
actual fun nowAsPlatformDate(): PlatformDate = PlatformDate(NSDate())
actual fun nowAsPlatformInstant(): PlatformInstant = PlatformInstant(NSDate())

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
actual val imageDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun getStacktrace(): String? = null

actual fun getPlatformName(): String {
    return when (Platform.osFamily) {
        OsFamily.IOS -> "iPhone"
        OsFamily.MACOSX -> "Macintosh"
        OsFamily.TVOS -> "tvOS"
        OsFamily.WATCHOS -> "watchOS"
        else -> "Apple Device"
    }
}
