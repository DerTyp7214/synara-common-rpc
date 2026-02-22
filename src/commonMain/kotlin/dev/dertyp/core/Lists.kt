@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.data.Album
import dev.dertyp.data.Artist

fun List<Artist>.joinArtists(): String = sortedBy { it.name }.joinToString(", ") { it.name }

fun List<Album>.parseVersions(): Pair<Album, List<Album>> {
    val sorted = sortedByDescending { it.songCount }
    val album = sorted.first()
    val versions = sorted.drop(1)

    return Pair(album, versions)
}

fun <T> Iterable<T>.minusOnce(other: Iterable<T>): List<T> {
    val result = toMutableList()
    other.forEach { result.remove(it) }
    return result
}

@Suppress("UNCHECKED_CAST")
fun <K, V> List<Pair<K, V?>>.filterValueNotNull(): List<Pair<K, V>> = filter { (_, v) -> v != null } as List<Pair<K, V>>

@Suppress("UNCHECKED_CAST")
fun <K, V> List<Pair<K?, V>>.filterKeyNotNull(): List<Pair<K, V>> = filter { (k, _) -> k != null } as List<Pair<K, V>>

@Suppress("UNCHECKED_CAST")
fun <K, V> List<Pair<K?, V?>>.filterNotNull(): List<Pair<K, V>> = filter { (k, v) -> k != null && v != null } as List<Pair<K, V>>

val <K, V> List<Pair<K, V>>.keys
    get() = map { it.first }
val <K, V> List<Pair<K, V>>.values
    get() = map { it.second }

fun <T> MutableList<T>.removeFirst(condition: (T) -> Boolean): T? {
    val index = indexOfFirst(condition)
    if (index > -1) return removeAt(index)

    return null
}

fun <K, V> List<Map.Entry<K, V>>.toMap(): Map<K, V> {
    val map = mutableMapOf<K, V>()
    forEach { entry ->
        map[entry.key] = entry.value
    }
    return map
}

fun <T> MutableList<T>.splice(
    start: Int,
    deleteCount: Int,
    vararg items: T
): List<T> {
    val actualStart = if (start < 0) size + start else start

    val removedElements = if (deleteCount > 0) {
        this.subList(actualStart, (actualStart + deleteCount).coerceAtMost(size)).let {
            val removed = it.toList()
            it.clear()
            removed
        }
    } else {
        emptyList()
    }

    if (items.isNotEmpty()) {
        this.addAll(actualStart, items.toList())
    }

    return removedElements
}
