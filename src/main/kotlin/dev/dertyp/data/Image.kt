package dev.dertyp.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Image(
    val id: @Contextual UUID,
    val path: String,
    val imageHash: String,
    val origin: String,
)

@Serializable
data class InsertableImage(
    val data: ByteArray,
    val imageHash: String,
    val origin: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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
