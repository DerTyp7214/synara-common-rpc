@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services.models

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class FollowedArtist(
    val artistId: PlatformUUID,
    val name: String,
    val imageId: PlatformUUID? = null
)
