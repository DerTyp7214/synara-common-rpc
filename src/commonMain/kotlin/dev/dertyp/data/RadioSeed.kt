@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Seed material for a radio station. When any field is set, the radio is built from songs similar to the seed rather than from listen history.")
data class RadioSeed(
    @FieldDoc("Explicit seed song unique identifiers.")
    val songIds: List<PlatformUUID> = emptyList(),
    @FieldDoc("Seed from all songs in a playlist.")
    val playlistId: PlatformUUID? = null,
    @FieldDoc("Seed from all songs in an album.")
    val albumId: PlatformUUID? = null,
    @FieldDoc("Seed from an artist's songs.")
    val artistId: PlatformUUID? = null,
) {
    fun isEmpty(): Boolean =
        songIds.isEmpty() && playlistId == null && albumId == null && artistId == null
}
