package dev.dertyp

/**
 * An ID that can optionally be prefixed with an importer ID (e.g., "<importerId>:123").
 */
typealias PrefixedId = String

fun PrefixedId.getPrefix(): String? = if (this.contains(":")) this.substringBefore(":") else null
fun PrefixedId.stripPrefix(): String = if (this.contains(":")) this.substringAfter(":") else this
