@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.UUID

@Serializable
data class PlaybackState(
    val queue: List<QueueEntry>,
    val currentIndex: Int,
    val isPlaying: Boolean,
    val positionMs: Long,
    val shuffleMode: Boolean,
    val repeatMode: String,
    val sourceId: String? = null
) {
    @Serializable
    sealed class QueueEntry {
        abstract val queueId: Long

        @Serializable
        @SerialName("FromSource")
        data class FromSource(
            val songId: UUID,
            override val queueId: Long
        ) : QueueEntry()

        @Serializable
        @SerialName("Explicit")
        data class Explicit(
            val song: UserSong,
            override val queueId: Long
        ) : QueueEntry()
    }
}
