package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ClientRequestStatus
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.QueueInfo
import dev.dertyp.data.QueueItem
import dev.dertyp.data.QueueMeta
import dev.dertyp.data.QueueSyncDevice
import dev.dertyp.data.QueueUploadStart
import dev.dertyp.data.QueueWriteResult
import dev.dertyp.data.RepeatMode
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Manages the server-stored play queue shared between the devices of a user. Every write carries the version the client based its change on; " +
        "if the server has moved on since then the write is rejected with a conflict carrying the current state, unless the client forces it."
)
interface IQueueService {
    @RestGet
    @RpcDoc("Get the metadata of the queue without its entries. A user without a stored queue is reported at version 0 with no entries.")
    suspend fun getQueueInfo(): QueueInfo

    @RestGet
    @RpcDoc(
        "Read a page of queue entries, ordered by their position in the original order. Each entry also carries its shuffled position, " +
            "and its song metadata only when the entries are requested with the songs resolved."
    )
    suspend fun getQueue(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 200,
        @RpcParamDoc("Resolve each entry's song; leave false for large queues and fetch songs separately.") includeSongs: Boolean = false
    ): PaginatedResponse<QueueItem>

    @RestGet
    @RpcDoc("Watch the queue of the user for changes. A new metadata snapshot is emitted after every successful write, including writes made by other devices.")
    fun observeQueue(): Flow<QueueInfo>

    @RpcDoc(
        "Start a chunked upload that replaces the whole queue. Entries are staged in memory with uploadPage and become visible only on commitUpload. " +
            "Starting an upload discards any previous staged upload of the user, and a staged upload expires after a while if it is not committed."
    )
    suspend fun beginUpload(
        @RpcParamDoc("The queue version the upload is based on.") baseVersion: Long,
        @RpcParamDoc("Whether to start the upload even if the server is past the base version.") force: Boolean = false
    ): QueueUploadStart

    @RpcDoc("Stage another page of entries for a started upload. Returns the total number of entries staged so far.")
    suspend fun uploadPage(
        @RpcParamDoc("The upload identifier returned by beginUpload.") uploadId: PlatformUUID,
        @RpcParamDoc("The entries of this page, in upload order.") items: List<QueueItem>
    ): Int

    @RpcDoc(
        "Replace the queue with the staged entries and store the supplied playback metadata. Entries referring to unknown songs are dropped and both orders are renumbered. " +
            "When the upload answers a queue request of another device, its request identifier is passed so the requester is notified."
    )
    suspend fun commitUpload(
        @RpcParamDoc("The upload identifier returned by beginUpload.") uploadId: PlatformUUID,
        @RpcParamDoc("The playback metadata to store with the queue.") meta: QueueMeta,
        @RpcParamDoc("The client request this upload answers, if any.") requestId: PlatformUUID? = null
    ): QueueWriteResult

    @RpcDoc("Discard a staged upload without touching the stored queue.")
    suspend fun cancelUpload(
        @RpcParamDoc("The upload identifier returned by beginUpload.") uploadId: PlatformUUID
    )

    @RpcDoc(
        "Insert entries into the queue at a position of the active order (the shuffled order while shuffle is on, otherwise the original order). " +
            "With shuffle on the entries are spliced into the shuffled order and appended to the end of the original order."
    )
    suspend fun insert(
        @RpcParamDoc("The queue version the change is based on.") baseVersion: Long,
        @RpcParamDoc("Zero-based index in the active order at which the entries are inserted.") position: Int,
        @RpcParamDoc("The entries to insert, in the order they should appear.") items: List<QueueItem>,
        @RpcParamDoc("Whether to apply the change even if the server is past the base version.") force: Boolean = false
    ): QueueWriteResult

    @RpcDoc("Remove entries from the queue by their queue identifiers. Unknown identifiers are ignored and the current index is adjusted to stay on the playing entry.")
    suspend fun remove(
        @RpcParamDoc("The queue version the change is based on.") baseVersion: Long,
        @RpcParamDoc("The queue identifiers of the entries to remove.") queueIds: List<Long>,
        @RpcParamDoc("Whether to apply the change even if the server is past the base version.") force: Boolean = false
    ): QueueWriteResult

    @RpcDoc("Move a single entry to another position of the active order (the shuffled order while shuffle is on, otherwise the original order).")
    suspend fun move(
        @RpcParamDoc("The queue version the change is based on.") baseVersion: Long,
        @RpcParamDoc("The queue identifier of the entry to move.") queueId: Long,
        @RpcParamDoc("Zero-based target index in the active order.") toPosition: Int,
        @RpcParamDoc("Whether to apply the change even if the server is past the base version.") force: Boolean = false
    ): QueueWriteResult

    @RpcDoc("Set the entry that is currently playing, addressed by its index in the active order. The index is clamped to the size of the queue.")
    suspend fun setCurrentIndex(
        @RpcParamDoc("The queue version the change is based on.") baseVersion: Long,
        @RpcParamDoc("Zero-based index in the active order.") currentIndex: Int,
        @RpcParamDoc("Whether to apply the change even if the server is past the base version.") force: Boolean = false
    ): QueueWriteResult

    @RpcDoc(
        "Set shuffle and repeat mode. Enabling shuffle makes the server generate a shuffled order that starts with the entry that is currently playing, " +
            "so the current index becomes 0; disabling it drops the shuffled order and puts the current index back on the original position of that entry. " +
            "A player that shuffled on its own uploads its order instead."
    )
    suspend fun setModes(
        @RpcParamDoc("The queue version the change is based on.") baseVersion: Long,
        @RpcParamDoc("Whether the queue is played in shuffled order.") shuffleMode: Boolean,
        @RpcParamDoc("The repetition mode to store.") repeatMode: RepeatMode,
        @RpcParamDoc("Whether to apply the change even if the server is past the base version.") force: Boolean = false
    ): QueueWriteResult

    @RpcDoc("Opt the calling session in or out of queue synchronization and record the name it is listed under on other devices.")
    suspend fun setSyncEnabled(
        @RpcParamDoc("Whether this device takes part in queue synchronization.") enabled: Boolean,
        @RpcParamDoc("Human readable name of this device.") deviceName: String
    )

    @RpcDoc("Record that the calling session pulled the queue up to a version, so it can tell later whether the server state is newer than its own.")
    suspend fun ackSynced(
        @RpcParamDoc("The queue version this device is now in sync with.") version: Long
    )

    @RestGet
    @RpcDoc("List the devices of the user that take part in queue synchronization, including which entry is the calling session.")
    suspend fun getSyncDevices(): List<QueueSyncDevice>

    @RpcDoc(
        "Ask another device of the same user to upload its current queue and wait for the outcome. The request is delivered over the client request channel; " +
            "sessions that are not listening are reported as unreachable and sessions that stay silent as timed out."
    )
    suspend fun requestUploadFrom(
        @RpcParamDoc("The session unique identifier of the device to ask.") sessionId: PlatformUUID
    ): ClientRequestStatus
}
