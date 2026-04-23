@file:JvmName("CommonAlbum")

package dev.dertyp.core

import dev.dertyp.data.InsertableAlbum
import kotlin.jvm.JvmName

fun InsertableAlbum.contentEquals(other: InsertableAlbum): Boolean {
    if (this.originalId != other.originalId) return false
    if (this.originalId != null) return true
    return this.name == other.name &&
            this.artists.sorted() == other.artists.sorted() &&
            this.releaseDate == other.releaseDate
}
