@file:Suppress("unused")

package dev.dertyp.core

private val titleCleanRegex = Regex(
    """\s*([(\[].*?(feat|ft|with|prod|live|remix|acoustic|radio\sedit|explicit|clean|remaster).*?[)\]])|\s+(feat|ft|with|prod)\.?\s+.*$""",
    RegexOption.IGNORE_CASE
)

fun String.cleanTitle(): String {
    return this.replace(titleCleanRegex, "").trim()
}

fun String.prefixIfNotBlank(prefix: String): String = if (isNotBlank()) "$prefix$this" else this
