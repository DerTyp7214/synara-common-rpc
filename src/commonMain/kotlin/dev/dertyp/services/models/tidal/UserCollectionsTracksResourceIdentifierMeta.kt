package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class UserCollectionsTracksResourceIdentifierMeta(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val addedAt: PlatformOffsetDateTime
)
