@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
data class Artist(
    val id: UUID,
    val name: String,
    val isGroup: Boolean,
    val artists: List<Artist> = listOf(),
    val about: String = "",
    val imageId: UUID? = null,
)


@Serializable
data class MergeArtists(
    val name: String,
    val image: String? = null,
    val artistIds: List<UUID>,
)