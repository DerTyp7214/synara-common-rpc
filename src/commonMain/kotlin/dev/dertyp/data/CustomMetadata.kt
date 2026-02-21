package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomMetadata(
    val title: String? = null,
    val artists: List<String>? = null,
    val album: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val coverData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomMetadata

        if (title != other.title) return false
        if (artists != other.artists) return false
        if (album != other.album) return false
        if (year != other.year) return false
        if (genre != other.genre) return false
        if (coverData != null) {
            if (other.coverData == null) return false
            if (!coverData.contentEquals(other.coverData)) return false
        } else if (other.coverData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artists?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (coverData?.contentHashCode() ?: 0)
        return result
    }
}
