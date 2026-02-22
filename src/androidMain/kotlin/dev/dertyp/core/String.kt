package dev.dertyp.core

import java.text.Normalizer

actual fun String.stripAccents(): String {
    val normalizer = Normalizer.normalize(this, Normalizer.Form.NFD)
    val accentRegex = Regex("\\p{InCombiningDiacriticalMarks}+")
    return accentRegex.replace(normalizer, "")
}
