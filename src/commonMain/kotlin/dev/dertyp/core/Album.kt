@file:JvmName("CommonAlbum")

package dev.dertyp.core

import dev.dertyp.data.InsertableAlbum
import kotlin.jvm.JvmName

fun InsertableAlbum.contentEquals(other: InsertableAlbum): Boolean {
    return name == other.name &&
            artists.sorted() == other.artists.sorted() &&
            releaseDate == other.releaseDate
}
