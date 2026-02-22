package dev.dertyp.core

actual fun String.stripAccents(): String {
    val map = mapOf(
        'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
        'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e',
        'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i',
        'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o',
        'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u',
        'ñ' to 'n', 'ç' to 'c'
    )
    return this.map { map[it.lowercaseChar()]?.let { m -> if (it.isUpperCase()) m.uppercaseChar() else m } ?: it }.joinToString("")
}
