package dev.dertyp.services

import dev.dertyp.data.ListenBrainzStatus
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manage the current user's ListenBrainz connection and listen-history sync.")
interface IListenBrainzService {
    @RpcDoc("Link the current user to a ListenBrainz account and trigger an initial sync.")
    suspend fun link(
        @RpcParamDoc("The ListenBrainz username.") username: String,
        @RpcParamDoc("Optional ListenBrainz user token, required for private listens.") token: String? = null
    ): ListenBrainzStatus

    @RpcDoc("Remove the current user's ListenBrainz link.")
    suspend fun unlink()

    @RpcDoc("Get the current user's ListenBrainz connection status, or null if not linked.")
    suspend fun getStatus(): ListenBrainzStatus?

    @RpcDoc("Stream the current user's ListenBrainz status, re-emitting whenever it changes (e.g. during a sync).")
    fun getStatusFlow(): Flow<ListenBrainzStatus?>

    @RpcDoc("Trigger an incremental sync of the current user's ListenBrainz listens now.")
    suspend fun syncNow(): ListenBrainzStatus
}
