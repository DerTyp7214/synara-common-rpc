@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Metadata describing the state of the server-stored play queue of a user.")
data class QueueInfo(
    @FieldDoc("Monotonically increasing revision of the queue, incremented by every successful write. A fresh queue is at version 0.")
    val version: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last successful write.")
    val modifiedAt: Long,
    @FieldDoc("The session that performed the last write, or null if the queue was never written.")
    val modifiedBySessionId: PlatformUUID? = null,
    @FieldDoc("Snapshot of the device name of the session that performed the last write.")
    val modifiedByDeviceName: String? = null,
    @FieldDoc("Index of the currently playing entry in the active order (the shuffled order while shuffle is on, otherwise the original order).")
    val currentIndex: Int,
    @FieldDoc("Whether the queue is played in the stored shuffled order.")
    val shuffleMode: Boolean,
    @FieldDoc("The current repetition mode.")
    val repeatMode: RepeatMode,
    @FieldDoc("Identifier for the origin of the queue (e.g., a playlist ID).")
    val sourceId: String? = null,
    @FieldDoc("Total number of entries in the queue.")
    val total: Int
)

@Serializable
@ModelDoc("A single entry of the server-stored play queue.")
data class QueueItem(
    @FieldDoc("The song unique identifier. Entries whose song no longer exists are dropped silently.")
    val songId: PlatformUUID,
    @FieldDoc("Identifier of this specific instance in the queue, unique per user. The same song may appear multiple times with different queue IDs.")
    val queueId: Long,
    @FieldDoc("Zero-based index in the original (unshuffled) order.")
    val position: Int,
    @FieldDoc("Zero-based index in the shuffled order, or null while shuffle is off.")
    val shuffledPosition: Int? = null,
    @FieldDoc("Whether this entry was added explicitly by the user rather than by the source.")
    val explicit: Boolean = false,
    @FieldDoc("The resolved song metadata. Filled in by the server only when the queue is read with includeSongs enabled, and ignored on writes.")
    val song: UserSong? = null
)

@Serializable
@ModelDoc("Playback metadata written together with a full queue upload.")
data class QueueMeta(
    @FieldDoc("Index of the currently playing entry in the active order.")
    val currentIndex: Int,
    @FieldDoc("Whether the queue is played in the uploaded shuffled order.")
    val shuffleMode: Boolean,
    @FieldDoc("The repetition mode to store.")
    val repeatMode: RepeatMode,
    @FieldDoc("Identifier for the origin of the queue (e.g., a playlist ID).")
    val sourceId: String? = null
)

@Serializable
@ModelDoc("Result of a queue write, either the applied new state or a version conflict.")
sealed class QueueWriteResult {
    @FieldDoc("The queue metadata after the write, or the current server state when the write was rejected.")
    abstract val info: QueueInfo

    @Serializable
    @SerialName("Ok")
    @ModelDoc("The write was applied and the queue advanced to a new version.")
    data class Ok(
        @FieldDoc("The queue metadata after the write.")
        override val info: QueueInfo
    ) : QueueWriteResult()

    @Serializable
    @SerialName("Conflict")
    @ModelDoc("The write was rejected because the server moved past the supplied base version. The client either pulls the server queue or repeats the write with force enabled.")
    data class Conflict(
        @FieldDoc("The unchanged current queue metadata on the server.")
        override val info: QueueInfo
    ) : QueueWriteResult()
}

@Serializable
@ModelDoc("Result of starting a chunked queue upload.")
sealed class QueueUploadStart {
    @Serializable
    @SerialName("Started")
    @ModelDoc("An upload slot was reserved and items can be staged with the returned upload ID.")
    data class Started(
        @FieldDoc("Identifier of the staged upload, passed to every subsequent page, commit or cancel call.")
        val uploadId: PlatformUUID,
        @FieldDoc("Unix timestamp in milliseconds after which the staged upload is discarded.")
        val expiresAt: Long
    ) : QueueUploadStart()

    @Serializable
    @SerialName("Conflict")
    @ModelDoc("No upload was started because the server moved past the supplied base version. The client either pulls the server queue or repeats the call with force enabled.")
    data class Conflict(
        @FieldDoc("The unchanged current queue metadata on the server.")
        val info: QueueInfo
    ) : QueueUploadStart()
}

@Serializable
@ModelDoc("A device of the user that participates in queue synchronization.")
data class QueueSyncDevice(
    @FieldDoc("The session unique identifier of the device.")
    val sessionId: PlatformUUID,
    @FieldDoc("Human readable name the device reported for itself.")
    val deviceName: String,
    @FieldDoc("Whether the device currently takes part in queue synchronization.")
    val enabled: Boolean,
    @FieldDoc("The queue version this device last pulled or wrote.")
    val lastSyncedVersion: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last synchronization of this device.")
    val lastSyncAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last activity of the session.")
    val lastActive: Long,
    @FieldDoc("Whether this entry describes the calling session.")
    val isCurrent: Boolean
)
