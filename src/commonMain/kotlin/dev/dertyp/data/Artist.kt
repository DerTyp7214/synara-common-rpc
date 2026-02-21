@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class Artist(
    val id: PlatformUUID,
    val name: String,
    val isGroup: Boolean,
    val artists: List<Artist> = listOf(),
    val about: String = "",
    val imageId: PlatformUUID? = null,
)


@Serializable
data class MergeArtists(
    val name: String,
    val image: String? = null,
    val artistIds: List<PlatformUUID>,
)
