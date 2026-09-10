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
}

@Serializable
@ModelDoc("Outcome of a server request sent to a client session.")
enum class ClientRequestStatus {
    @FieldDoc("The client performed the requested task.") COMPLETED,
    @FieldDoc("The client received the request but declined it.") REJECTED,
    @FieldDoc("The client did not answer before the server stopped waiting.") TIMED_OUT,
    @FieldDoc("The target session is not listening for requests.") UNREACHABLE
}
