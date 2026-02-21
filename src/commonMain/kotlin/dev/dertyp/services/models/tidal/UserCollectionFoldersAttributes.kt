package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class UserCollectionFoldersAttributes(
    val collectionType: CollectionType,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: PlatformOffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val lastModifiedAt: PlatformOffsetDateTime,
    val name: String
): BaseAttributes() {
    @Suppress("unused")
    enum class CollectionType {
        PLAYLISTS;
    }
}
