package dev.dertyp.services

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Lyrics metadata from LrcLib.")
data class LrcLibResponse(
    @FieldDoc("The LrcLib internal ID.")
    val id: Int,
    @FieldDoc("The name of the track.")
    val trackName: String,
    @FieldDoc("The name of the artist.")
    val artistName: String,
    @FieldDoc("The name of the album.")
    val albumName: String,
    @FieldDoc("Duration of the track in seconds.")
    val duration: Double,
    @FieldDoc("Whether the track is instrumental.")
    val instrumental: Boolean,
    @FieldDoc("The plain text lyrics.")
    val plainLyrics: String? = null,
    @FieldDoc("The synced lyrics in LRC format.")
    val syncedLyrics: String? = null
)

interface ILrcLibService {
    suspend fun getLyrics(artist: String, title: String, album: String? = null, duration: Long? = null): LrcLibResponse?
}
