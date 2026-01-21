package dev.dertyp.data

import dev.dertyp.serializers.UUIDByteListSerializer
import dev.dertyp.serializers.UUIDByteSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Artist(
    @Serializable(with = UUIDByteSerializer::class)
    val id: UUID,
    val name: String,
    val isGroup: Boolean,
    val artists: List<Artist> = listOf(),
    val about: String = "",
    @Serializable(with = UUIDByteSerializer::class)
    val imageId: UUID? = null,
)


@Serializable
data class MergeArtists(
    val name: String,
    val image: String? = null,
    @Serializable(with = UUIDByteListSerializer::class)
    val artistIds: List<UUID>,
)