@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import dev.dertyp.core.contentEquals
import dev.dertyp.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.time.LocalDate
import java.util.*

@Serializable
data class Album(
    val id: UUID,
    val name: String,
    val artists: List<Artist>,
    val songCount: Int = 0,
    @Serializable(with = LocalDateSerializer::class)
    val releaseDate: LocalDate?,
    val totalDuration: Long,
    val totalSize: Long = 0,
    val coverId: UUID? = null,
    val originalId: String? = null,
)

@Serializable
data class InsertableAlbum(
    val name: String,
    val artists: List<String>,
    @Serializable(with = LocalDateSerializer::class)
    val releaseDate: LocalDate? = null,
    val songCount: Int = 0,
    val coverHash: String? = null,
    val originalId: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        return if (other is InsertableAlbum) contentEquals(other) else false
    }

    override fun hashCode(): Int {
        var result = songCount
        result = 31 * result + name.hashCode()
        result = 31 * result + artists.sorted().joinToString(", ").hashCode()
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (originalId?.hashCode() ?: 0)
        return result
    }
}