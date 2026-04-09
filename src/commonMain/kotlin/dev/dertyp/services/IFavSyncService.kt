package dev.dertyp.services

import dev.dertyp.PlatformDate
import dev.dertyp.data.FavSync
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Tracks the history of favorite synchronization tasks.")
interface IFavSyncService {
    @RpcDoc("Get the timestamp of the last successful synchronization for a specific service.")
    suspend fun getLatestFavSync(
        @RpcParamDoc("The service type (e.g., TIDAL).") service: ISyncService.SyncServiceType
    ): FavSync?
    @RpcDoc("Record a new successful synchronization timestamp.")
    suspend fun insertFavSync(
        @RpcParamDoc("The service type.") service: ISyncService.SyncServiceType,
        @RpcParamDoc("The timestamp of synchronization.") syncedAt: PlatformDate
    ): Int
}
