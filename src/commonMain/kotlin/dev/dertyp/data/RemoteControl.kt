@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc(
    "What a remote-controllable device is playing, as it last reported it. A controlling device projects the current position from the position and the " +
        "report timestamp while the device is playing, instead of expecting a report for every second."
)
data class RemotePlaybackStatus(
    @FieldDoc("The song the device is playing, or null when it is not playing anything.")
    val songId: PlatformUUID? = null,
    @FieldDoc("Whether the device is playing right now rather than paused or stopped.")
    val isPlaying: Boolean,
    @FieldDoc("Playback position within the song in milliseconds at the time of the report.")
    val positionMs: Long,
    @FieldDoc("Length of the song in milliseconds, when the device knows it.")
    val durationMs: Long? = null,
    @FieldDoc("Whether the device plays its queue in shuffled order.")
    val shuffleMode: Boolean,
    @FieldDoc("The repetition mode of the device.")
    val repeatMode: RepeatMode,
    @FieldDoc("Playback volume between 0 and 1, or null on a device that does not expose its volume.")
    val volume: Float? = null,
    @FieldDoc("Queue id of the shared queue entry the device is playing, or null when the device is not playing its shared queue.")
    val currentQueueId: Long? = null,
    @FieldDoc("The version of the shared queue the device has applied, or null when it does not take part in queue sync.")
    val queueVersion: Long? = null,
    @FieldDoc("Unix timestamp in milliseconds at which the server received the report. Ignored on report and filled in by the server.")
    val reportedAt: Long = 0
)

@Serializable
@ModelDoc("A transport command sent to a remote-controllable device of the same user.")
sealed class PlaybackCommand {
    @Serializable
    @SerialName("Play")
    @ModelDoc("Start or resume playback on the device.")
    data object Play : PlaybackCommand()

    @Serializable
    @SerialName("Pause")
    @ModelDoc("Pause playback on the device.")
    data object Pause : PlaybackCommand()

    @Serializable
    @SerialName("TogglePlayPause")
    @ModelDoc("Pause the device while it plays and resume it while it is paused.")
    data object TogglePlayPause : PlaybackCommand()

    @Serializable
    @SerialName("Next")
    @ModelDoc("Skip to the next entry of the queue of the device.")
    data object Next : PlaybackCommand()

    @Serializable
    @SerialName("Previous")
    @ModelDoc("Go back to the previous entry of the queue of the device, or restart the current one, as the device decides.")
    data object Previous : PlaybackCommand()

    @Serializable
    @SerialName("SeekTo")
    @ModelDoc("Jump to a position within the song the device is playing.")
    data class SeekTo(
        @FieldDoc("Target position within the song in milliseconds.")
        val positionMs: Long
    ) : PlaybackCommand()

    @Serializable
    @SerialName("SetShuffle")
    @ModelDoc("Turn shuffled playback of the device on or off.")
    data class SetShuffle(
        @FieldDoc("Whether the device plays its queue in shuffled order.")
        val enabled: Boolean
    ) : PlaybackCommand()

    @Serializable
    @SerialName("SetRepeat")
    @ModelDoc("Set the repetition mode of the device.")
    data class SetRepeat(
        @FieldDoc("The repetition mode to apply.")
        val mode: RepeatMode
    ) : PlaybackCommand()

    @Serializable
    @SerialName("SetVolume")
    @ModelDoc("Set the playback volume of the device. Only accepted by devices that also offer the remote volume capability.")
    data class SetVolume(
        @FieldDoc("The volume to apply, between 0 and 1.")
        val volume: Float
    ) : PlaybackCommand()

    @Serializable
    @SerialName("PlayQueueItem")
    @ModelDoc(
        "Select and play the entry with this queue id of the shared queue of the user. The device first applies at least the given queue version of the " +
            "shared queue, pulling it when it has not seen it yet, and rejects the command when the entry is not in its queue afterwards, which is how a " +
            "controller loads a new queue and then plays a song in it without a race."
    )
    data class PlayQueueItem(
        @FieldDoc("The queue id of the entry within the shared queue.")
        val queueId: Long,
        @FieldDoc("The shared queue version that contains the entry, as returned to the controller by its queue write.")
        val queueVersion: Long
    ) : PlaybackCommand()
}
