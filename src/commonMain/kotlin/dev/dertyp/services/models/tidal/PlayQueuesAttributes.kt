package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PlayQueuesAttributes(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: PlatformOffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val lastModifiedAt: PlatformOffsetDateTime,
    val repeat: Repeat,
    val shuffled: Boolean
): BaseAttributes() {
    @Suppress("unused")
    enum class Repeat {
        NONE,
        ONE,
        BATCH;
    }
}
