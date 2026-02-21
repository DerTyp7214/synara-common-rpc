package dev.dertyp.services.models.tidal

import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.DurationSerializer
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class PlaylistsAttributes(
    val accessType: AccessType,
    val bounded: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: PlatformOffsetDateTime,
    val externalLinks: List<ExternalLink>,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val lastModifiedAt: PlatformOffsetDateTime,
    val name: String,
    val playlistType: PlaylistType,
    val description: String? = null,
    @Serializable(with = DurationSerializer::class)
    val duration: Duration,
    val numberOfItems: Int? = null
): BaseAttributes() {
    @Suppress("unused")
    enum class AccessType {
        PUBLIC,
        UNLISTED;
    }

    @Suppress("unused")
    enum class PlaylistType {
        EDITORIAL,
        USER,
        MIX,
        ARTIST;
    }
}
