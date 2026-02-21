@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

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
            val songId: PlatformUUID,
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
