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
