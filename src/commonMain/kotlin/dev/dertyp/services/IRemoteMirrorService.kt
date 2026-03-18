package dev.dertyp.services

import dev.dertyp.data.MirrorProgress
import dev.dertyp.data.RemoteServerConfig
import dev.dertyp.data.ServerStats
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IRemoteMirrorService {
    suspend fun getRemoteStats(config: RemoteServerConfig): ServerStats
    suspend fun startMirror(config: RemoteServerConfig)
    suspend fun stopMirror()
    suspend fun resetMirror()
    fun getActiveMirrorProgress(): Flow<MirrorProgress>?
}
