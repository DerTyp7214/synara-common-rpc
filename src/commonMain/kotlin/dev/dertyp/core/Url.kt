@file:JvmName("CommonUrl")
@file:Suppress("unused")

package dev.dertyp.core

import io.ktor.http.*
import kotlin.jvm.JvmName

fun Url.tidalId(): String = segments.last { s -> s != "u" }

fun safeParseUrl(url: String): Url? = try {
    Url(url)
} catch (_: Exception) {
    null
}
