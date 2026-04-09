@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services.models

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Represents an artist that a user is following for release tracking.")
data class FollowedArtist(
    @FieldDoc("The artist unique identifier.")
    val artistId: PlatformUUID,
    @FieldDoc("The name of the artist.")
    val name: String,
    @FieldDoc("The artist image unique identifier.")
    val imageId: PlatformUUID? = null
)
