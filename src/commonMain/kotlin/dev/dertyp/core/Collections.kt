@file:Suppress("unused")

package dev.dertyp.core

fun <T> List<T>.takeEvery(n: Int): List<T> {
    require(n > 0) { "n must be greater than 0" }
    return this.filterIndexed { index, _ -> index % n == 0 }
}

inline fun <reified T> Array<T>.takeEvery(n: Int): Array<T> {
    require(n > 0) { "n must be greater than 0" }
    return this.filterIndexed { index, _ -> index % n == 0 }.toTypedArray()
}

fun ByteArray.takeEvery(n: Int): ByteArray {
    return this.filterIndexed { index, _ -> index % n == 0 }.toByteArray()
}

fun <T> List<T>.nullIfEmpty(): List<T>? = ifEmpty { null }

operator fun <T> MutableList<T>.divAssign(new: Collection<T>) {
    clear()
    addAll(new)
}

infix fun <T> MutableList<T>.replaceWith(new: Collection<T>) {
    clear()
    addAll(new)
}
