package dev.dertyp.services

import dev.dertyp.PlatformDate
import dev.dertyp.data.FavSync
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IFavSyncService {
    suspend fun getLatestFavSync(service: ISyncService.SyncServiceType): FavSync?
    suspend fun insertFavSync(service: ISyncService.SyncServiceType, syncedAt: PlatformDate): Int
}
