@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("External provider information for a song or album.")
data class ProviderEntry(
    @FieldDoc("The provider name (e.g. spotify, youtube).")
    val provider: String,
    @FieldDoc("The unique identifier on the external platform.")
    val externalId: String,
    @FieldDoc("The type of the item on the external platform (e.g. track, album).")
    val type: String?,
    @FieldDoc("The original raw URL.")
    val rawUrl: String,
    @FieldDoc("Timestamp of when this entry was added.")
    val addedAt: Long
)

@Serializable
@ModelDoc("Extended metadata for a song.")
data class SongExtendedMetadata(
    @FieldDoc("List of all external providers and IDs matching this song.")
    val providers: List<ProviderEntry>,
    @FieldDoc("Detailed audio analysis data.")
    val audioData: SongAudioData?,
    @FieldDoc("Timestamp of when the song was first inserted into the database.")
    val insertedAt: Long
)

@Serializable
@ModelDoc("Extended metadata for an album.")
data class AlbumExtendedMetadata(
    @FieldDoc("List of all external providers and IDs matching this album.")
    val providers: List<ProviderEntry>
)
