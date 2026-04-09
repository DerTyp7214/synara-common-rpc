@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Modes for track repetition in the playback queue.")
enum class RepeatMode {
    @FieldDoc("Repeat disabled.") OFF,
    @FieldDoc("Repeat the entire queue.") ALL,
    @FieldDoc("Repeat the current track.") ONE
}

@Serializable
@ModelDoc("Represents the real-time status of music playback on a device.")
data class PlaybackState(
    @FieldDoc("Collection of song entries in the current queue.")
    val queue: List<QueueEntry>,
    @FieldDoc("Index of the currently playing song in the queue.")
    val currentIndex: Int,
    @FieldDoc("Whether the playback is currently active.")
    val isPlaying: Boolean,
    @FieldDoc("Current playback position in milliseconds.")
    val positionMs: Long,
    @FieldDoc("Whether the queue is being played in random order.")
    val shuffleMode: Boolean,
    @FieldDoc("The current repetition mode.")
    val repeatMode: RepeatMode,
    @FieldDoc("Identifier for the origin of the current queue (e.g., a playlist ID).")
    val sourceId: String? = null
) {
    @Serializable
    @ModelDoc("Base class for entries in the playback queue.")
    sealed class QueueEntry {
        @FieldDoc("A unique identifier for this specific instance in the queue.")
        abstract val queueId: Long

        @Serializable
        @SerialName("FromSource")
        @ModelDoc("A queue entry linked to a persistent song ID.")
        data class FromSource(
            @FieldDoc("The song unique identifier.")
            val songId: PlatformUUID,
            @FieldDoc("A unique identifier for this specific instance in the queue.")
            override val queueId: Long
        ) : QueueEntry()

        @Serializable
        @SerialName("Explicit")
        @ModelDoc("A queue entry containing full song metadata.")
        data class Explicit(
            @FieldDoc("The complete song metadata.")
            val song: UserSong,
            @FieldDoc("A unique identifier for this specific instance in the queue.")
            override val queueId: Long
        ) : QueueEntry()
    }
}
