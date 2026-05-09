@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Metadata for a stored image file (e.g., cover art, profile picture).")
data class Image(
    @FieldDoc("The image unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("Internal server path to the image file.")
    val path: String,
    @FieldDoc("Unique hash of the image content.")
    val imageHash: String,
    @FieldDoc("The source or platform where the image originated.")
    val origin: String,
    @FieldDoc("BlurHash string for placeholders.")
    val blurHash: String? = null,
    @FieldDoc("Width of the image in pixels.")
    val width: Int? = null,
    @FieldDoc("Height of the image in pixels.")
    val height: Int? = null,
    @FieldDoc("Size of the image file in bytes.")
    val byteSize: Long? = null,
    @FieldDoc("Primary color of the image in ARGB format.")
    val primaryColor: Int? = null,
    @FieldDoc("Perceived brightness of the image (0.0 to 1.0).")
    val luminance: Double? = null,
    @FieldDoc("Top 5 dominant colors in the image.")
    val palette: List<Int>? = null,
)

@Serializable
@ModelDoc("Configuration for uploading or storing a new image.")
data class InsertableImage(
    @FieldDoc("The raw binary data of the image.")
    val data: ByteArray,
    @FieldDoc("Unique hash of the image content.")
    val imageHash: String,
    @FieldDoc("The source or platform where the image originated.")
    val origin: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InsertableImage

        if (!data.contentEquals(other.data)) return false
        if (imageHash != other.imageHash) return false
        if (origin != other.origin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + imageHash.hashCode()
        result = 31 * result + origin.hashCode()
        return result
    }
}
