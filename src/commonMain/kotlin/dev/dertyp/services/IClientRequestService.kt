package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ClientRequest
import dev.dertyp.data.ClientRequestStatus
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Channel for tasks the server asks the calling client to perform. A client subscribes once, performs the tasks it receives through the regular RPC services " +
        "and reports the outcome back, which unblocks the device that asked for it."
)
interface IClientRequestService {
    @RestGet
    @RpcDoc("Watch for requests addressed to the calling session. A session that is not subscribed is reported as unreachable to the requester.")
    fun observeRequests(): Flow<ClientRequest>

    @RpcDoc("Report the outcome of a request to the waiting requester. Only COMPLETED and REJECTED are accepted; the timed out and unreachable outcomes are determined by the server.")
    suspend fun complete(
        @RpcParamDoc("The identifier of the request that was handled.") requestId: PlatformUUID,
        @RpcParamDoc("Whether the request was performed or declined.") status: ClientRequestStatus
    )
}
