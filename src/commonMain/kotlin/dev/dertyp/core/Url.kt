@file:JvmName("CommonUrl")
@file:Suppress("unused")

package dev.dertyp.core

import io.ktor.http.Url
import kotlin.jvm.JvmName

fun Url.tidalId(): String = segments.last { s -> s != "u" }

fun safeParseUrl(url: String): Url? {
    if (!url.contains("://")) return null
    val hostPart = url.substringAfter("://").substringBefore('/')
    if (hostPart.isBlank()) return null

    return try {
        val parsed = Url(url)
        if (parsed.protocol.name.isNotBlank() && parsed.host.isNotBlank()) parsed else null
    } catch (_: Exception) {
        null
    }
}
