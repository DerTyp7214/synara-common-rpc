package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class AppreciationsAttributes(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: PlatformOffsetDateTime
): BaseAttributes()
