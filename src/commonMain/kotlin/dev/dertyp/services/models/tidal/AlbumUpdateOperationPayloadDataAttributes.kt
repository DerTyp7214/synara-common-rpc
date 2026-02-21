package dev.dertyp.services.models.tidal

import dev.dertyp.PlatformLocalDate
import dev.dertyp.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AlbumUpdateOperationPayloadDataAttributes(
    val copyright: Copyright? = null,
    val explicitLyrics: Boolean? = null,
    @Serializable(with = LocalDateSerializer::class)
    val releaseDate: PlatformLocalDate? = null,
    val title: String? = null,
    val version: String? = null
): BaseAttributes()