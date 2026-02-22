@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.PlatformUUID
import dev.dertyp.platformUUIDFromString
import io.ktor.http.Url

private val titleCleanRegex = Regex(
    """\s*([(\[].*?(feat|ft|with|prod|live|remix|acoustic|radio\sedit|explicit|clean|remaster).*?[)\]])|\s+(feat|ft|with|prod)\.?\s+.*$""",
    RegexOption.IGNORE_CASE
)

fun String.cleanTitle(): String {
    return this.replace(titleCleanRegex, "").trim()
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
    return try {
        Url(this)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

fun String.capitalize(): String = replaceFirstChar { it.lowercase() }
fun String.oneLine(joiner: String = ""): String = split(Regex("[\n\r]")).joinToString(joiner)
fun String.tidalId(): String = Url(this).tidalId()

expect fun String.stripAccents(): String
