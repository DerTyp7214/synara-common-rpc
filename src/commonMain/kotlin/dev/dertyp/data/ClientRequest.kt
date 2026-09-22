@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A task the server asks a specific client session to perform. Clients observe these and answer by calling the matching RPC methods.")
sealed class ClientRequest {
    @FieldDoc("Identifier of this request, reported back when the client completes it.")
    abstract val id: PlatformUUID

    @FieldDoc("Unix timestamp in milliseconds at which the request was issued.")
    abstract val requestedAt: Long

    @Serializable
    @SerialName("UploadQueue")
    @ModelDoc(
        "Another device asks this client for its current play queue. The client fulfils the request by calling beginUpload with force set to true, " +
            "staging its entries with uploadPage and finishing with commitUpload passing this request ID, since the requester explicitly wants the queue of this device " +
            "and therefore expects it to win over the server state. If the client refuses, it reports the request as rejected instead."
    )
    data class UploadQueue(
        @FieldDoc("Identifier of this request, passed to commitUpload or complete.")
        override val id: PlatformUUID,
        @FieldDoc("Unix timestamp in milliseconds at which the request was issued.")
        override val requestedAt: Long,
        @FieldDoc("The session of the same user that asked for the queue.")
        val requestedBySessionId: PlatformUUID,
        @FieldDoc("Name of the device that asked for the queue, if known.")
        val requestedByDeviceName: String? = null
    ) : ClientRequest()

    @Serializable
    @SerialName("ControlPlayback")
    @ModelDoc(
        "Another device of the same user asks this client to change its playback. The client applies the command, publishes its new status through " +
            "the remote control service and answers with complete, reporting the request as rejected when it does not apply the command. " +
            "This request is only sent to sessions that connected with the remote control capability."
    )
    data class ControlPlayback(
        @FieldDoc("Identifier of this request, passed to complete.")
        override val id: PlatformUUID,
        @FieldDoc("Unix timestamp in milliseconds at which the request was issued.")
        override val requestedAt: Long,
        @FieldDoc("The session of the same user that asked for the change.")
        val requestedBySessionId: PlatformUUID,
        @FieldDoc("Name of the device that asked for the change, if known.")
        val requestedByDeviceName: String? = null,
        @FieldDoc("The transport command to apply.")
        val command: PlaybackCommand
    ) : ClientRequest()
}

@Serializable
@ModelDoc("Outcome of a server request sent to a client session.")
enum class ClientRequestStatus {
    @FieldDoc("The client performed the requested task.") COMPLETED,
    @FieldDoc("The client received the request but declined it.") REJECTED,
    @FieldDoc("The client did not answer before the server stopped waiting.") TIMED_OUT,
    @FieldDoc("The target session is not listening for requests.") UNREACHABLE
}

@Serializable
@ModelDoc("A cross-device feature a client offers for as long as it keeps its request subscription open.")
enum class ClientCapability {
    @FieldDoc("The client keeps the shared play queue in sync and can be asked to upload its own queue.") QUEUE_SYNC,
    @FieldDoc("The client accepts transport commands from other devices of the user and reports what it is playing.") REMOTE_CONTROL,
    @FieldDoc("The client can change its playback volume on request, which is not possible on every platform.") REMOTE_VOLUME
}

@Serializable
@ModelDoc("How a client describes itself when it connects to the request channel. The description is kept only while the connection is open, so a client resubscribes to change it.")
data class ClientDescription(
    @FieldDoc("Human readable name the device is listed under on the other devices of the user.")
    val deviceName: String,
    @FieldDoc("Free-form platform label of the device, for example Android, iOS or Desktop.")
    val platform: String = "",
    @FieldDoc("The settings synchronization device identifier of the client, when it has one, so other devices can correlate the connection with stored settings.")
    val deviceId: String? = null,
    @FieldDoc("The cross-device features this client offers while the connection is open.")
    val capabilities: Set<ClientCapability> = emptySet()
)

@Serializable
@ModelDoc("A device of the calling user that is connected to the request channel right now. Presence is not stored, so an entry exists exactly while that device keeps its connection open.")
data class OnlineDevice(
    @FieldDoc("The session unique identifier of the connected device, used to address it for remote control.")
    val sessionId: PlatformUUID,
    @FieldDoc("Human readable name the device reported for itself.")
    val deviceName: String,
    @FieldDoc("Free-form platform label the device reported for itself.")
    val platform: String = "",
    @FieldDoc("The settings synchronization device identifier the device reported, if any.")
    val deviceId: String? = null,
    @FieldDoc("The cross-device features the device offers on this connection.")
    val capabilities: Set<ClientCapability>,
    @FieldDoc("Whether this entry describes the calling session.")
    val isCurrent: Boolean,
    @FieldDoc("Unix timestamp in milliseconds at which the device connected.")
    val connectedAt: Long
)
