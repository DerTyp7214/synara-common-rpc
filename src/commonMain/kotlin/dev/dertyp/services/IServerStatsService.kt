package dev.dertyp.services

import dev.dertyp.data.ServerStats
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IServerStatsService {
    suspend fun getStats(): ServerStats
    suspend fun health(): Boolean
}