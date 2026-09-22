package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ClientDescription
import dev.dertyp.data.ClientRequest
import dev.dertyp.data.ClientRequestStatus
import dev.dertyp.data.OnlineDevice
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "The connection a client keeps open to the server for the lifetime of the app. It carries the tasks the server asks the calling client to perform, " +
        "which the client performs through the regular RPC services and reports back so the device that asked for it is unblocked, and it doubles as " +
        "the presence of the client: a session that connects with a description is listed as an online device of the user, with the cross-device " +
        "features it offers, for exactly as long as it keeps the stream open."
)
interface IClientRequestService {
    @RestGet
    @RpcDoc("Watch for requests addressed to the calling session without announcing the device. A session that is not subscribed is reported as unreachable to the requester.")
    fun observeRequests(): Flow<ClientRequest>

    @RestGet
    @RpcDoc(
        "Watch for requests addressed to the calling session and be listed as an online device of the user while the stream is open. The description " +
            "is kept only for the duration of the stream, so a client that changes its name or its capabilities subscribes again. Requests that need " +
            "a capability are only delivered to sessions that connected offering it."
    )
    fun connect(
        @RpcParamDoc("How this client is listed on the other devices of the user and what it can do.") description: ClientDescription
    ): Flow<ClientRequest>

    @RpcDoc("Report the outcome of a request to the waiting requester. Only COMPLETED and REJECTED are accepted; the timed out and unreachable outcomes are determined by the server.")
    suspend fun complete(
        @RpcParamDoc("The identifier of the request that was handled.") requestId: PlatformUUID,
        @RpcParamDoc("Whether the request was performed or declined.") status: ClientRequestStatus
    )

    @RestGet
    @RpcDoc("List the devices of the user that are connected right now, newest connection first. Sessions that only observe requests without a description are not listed.")
    suspend fun getOnlineDevices(): List<OnlineDevice>
}
