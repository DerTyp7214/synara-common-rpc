@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Metadata for a stored animated cover (a short looping video, e.g. motion album art).")
data class AnimatedImage(
    @FieldDoc("The animated image unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("Internal server path to the animated image file.")
    val path: String,
    @FieldDoc("Unique hash of the animated image content.")
    val contentHash: String,
    @FieldDoc("The source or platform where the animated image originated.")
    val origin: String,
    @FieldDoc("Container/format of the animated image (e.g. mp4, webm).")
    val format: String? = null,
    @FieldDoc("Identifier of the still Image extracted from the first frame.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("BlurHash of the first frame, surfaced for placeholders.")
    val blurHash: String? = null,
)

@Serializable
@ModelDoc("Configuration for uploading or storing a new animated image.")
data class InsertableAnimatedImage(
    @FieldDoc("The raw binary data of the animated image.")
    val data: ByteArray,
    @FieldDoc("Unique hash of the animated image content.")
    val contentHash: String,
    @FieldDoc("The source or platform where the animated image originated.")
    val origin: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InsertableAnimatedImage

        if (!data.contentEquals(other.data)) return false
        if (contentHash != other.contentHash) return false
        if (origin != other.origin) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + contentHash.hashCode()
        result = 31 * result + origin.hashCode()
        return result
    }
}
