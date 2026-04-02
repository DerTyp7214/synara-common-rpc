@file:JvmName("CommonString")
@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.PlatformUUID
import dev.dertyp.platformUUIDFromString
import io.ktor.http.Url
import kotlin.jvm.JvmName

private val titleCleanRegex = Regex(
    """\s*([(\[][^()\[\]]*(feat|ft|with|prod|live|remix|demo|acoustic|radio\sedit|album\sversion|immortal\sversion|single\sversion|explicit|clean|remaster)[^()\[\]]*[)\]])|\s+(feat|ft|with|prod)\.?\s+.*$""",
    RegexOption.IGNORE_CASE
)

fun String.cleanTitle(): String {
    var current = this
    var last: String
    do {
        last = current
        current = current.replace(titleCleanRegex, "").trim()
    } while (current != last)
    return current
}

fun String.prefixIfNotBlank(prefix: String): String = if (isNotBlank()) "$prefix$this" else this

fun String.toUUIDOrNull(): PlatformUUID? {
    return try {
        platformUUIDFromString(this)
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun String.isURL(): Boolean {
    if (!this.contains("://")) return false
    val hostPart = this.substringAfter("://").substringBefore('/')
    if (hostPart.isBlank()) return false

    return try {
        val url = Url(this)
        url.protocol.name.isNotBlank() && url.host.isNotBlank()
    } catch (_: Exception) {
        false
    }
}

fun String.capitalize(): String = lowercase().replaceFirstChar { it.uppercase() }
fun String.oneLine(joiner: String = ""): String = split(Regex("[\n\r]")).joinToString(joiner)
fun String.tidalId(): String = Url(this).tidalId()

expect fun String.stripAccents(): String
