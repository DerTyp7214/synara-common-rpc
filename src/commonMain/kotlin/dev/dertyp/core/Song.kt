package dev.dertyp.core

import dev.dertyp.data.*

@Suppress("UNCHECKED_CAST")
fun <T : BaseSong> PaginatedResponse<T>.omitLyrics() = PaginatedResponse(
    data = data.map {
        when (it) {
            is Song -> it.omitLyrics()
            is UserSong -> it.omitLyrics()
            else -> it
        }
    } as List<T>,
    total = total,
    page = page,
    pageSize = pageSize,
    hasNextPage = hasNextPage,
)

fun Song.omitLyrics(): Song = copy(
    lyrics = ""
)

fun UserSong.omitLyrics(): UserSong = copy(
    lyrics = ""
)

fun InsertableSong.contentEquals(other: InsertableSong): Boolean {
    return title == other.title &&
            explicit == other.explicit &&
            trackNumber == other.trackNumber &&
            discNumber == other.discNumber &&
            duration == other.duration &&
            album.name == other.album.name &&
            releaseDate == other.releaseDate
}
