package dev.dertyp.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Artist(
    val id: @Contextual UUID,
    val name: String,
    val isGroup: Boolean,
    val artists: List<Artist> = listOf(),
    val about: String = "",
    val imageId: @Contextual UUID? = null,
)


@Serializable
data class MergeArtists(
    val name: String,
    val image: String? = null,
    val artistIds: List<@Contextual UUID>,
)