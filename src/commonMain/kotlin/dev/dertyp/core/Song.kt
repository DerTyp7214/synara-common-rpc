@file:JvmName("CommonSong")

package dev.dertyp.core

import dev.dertyp.data.*
import kotlin.jvm.JvmName

fun InsertableSong.contentEquals(other: InsertableSong): Boolean {
    return title == other.title &&
        explicit == other.explicit &&
        trackNumber == other.trackNumber &&
        discNumber == other.discNumber &&
        duration == other.duration &&
        album.name == other.album.name &&
        album.tags == other.album.tags &&
        releaseDate == other.releaseDate &&
        tags == other.tags
}
