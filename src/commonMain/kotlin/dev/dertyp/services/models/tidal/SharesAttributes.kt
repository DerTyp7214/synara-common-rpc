package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class SharesAttributes(
    val code: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: PlatformOffsetDateTime,
    val externalLinks: List<ExternalLink>? = null
)
