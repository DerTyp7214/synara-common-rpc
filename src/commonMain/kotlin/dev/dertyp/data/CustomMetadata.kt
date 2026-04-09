package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Manually provided metadata for custom audio uploads.")
data class CustomMetadata(
    @FieldDoc("The title of the song.")
    val title: String? = null,
    @FieldDoc("Collection of artist names.")
    val artists: List<String>? = null,
    @FieldDoc("The name of the album.")
    val album: String? = null,
    @FieldDoc("The release year.")
    val year: String? = null,
    @FieldDoc("The musical genre.")
    val genre: String? = null,
    @FieldDoc("The raw binary data of the cover image.")
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
