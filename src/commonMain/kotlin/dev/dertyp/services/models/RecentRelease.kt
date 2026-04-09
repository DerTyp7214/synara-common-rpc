@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services.models

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.data.ReleaseType
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class RecentRelease(
    val releaseId: PlatformUUID,
    val artistId: PlatformUUID,
    val artistName: String,
    val title: String,
    @Serializable(with = DateSerializer::class)
    val releaseDate: PlatformDate?,
    val type: ReleaseType,
    val imageId: PlatformUUID? = null,
    val links: List<String> = emptyList(),
    val albumId: PlatformUUID? = null,
    val songId: PlatformUUID? = null
)
