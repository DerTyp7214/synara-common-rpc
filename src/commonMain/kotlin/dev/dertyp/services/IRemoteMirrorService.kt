package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IRemoteMirrorService {
    suspend fun getRemoteStats(config: RemoteServerConfig): ServerStats
    suspend fun startMirror(config: RemoteServerConfig)
    suspend fun stopMirror()
    suspend fun resetMirror()
    fun getActiveMirrorProgress(): Flow<MirrorProgress>
    suspend fun getRemoteUsers(config: RemoteServerConfig): List<User>
    suspend fun getRemotePlaylists(config: RemoteServerConfig): List<Playlist>
    suspend fun getRemoteUserPlaylists(config: RemoteServerConfig): List<UserPlaylist>
    suspend fun getProxyInstances(config: RemoteServerConfig): List<ProxyInstanceInfo>
    suspend fun getRemoteImageData(config: RemoteServerConfig, imageId: PlatformUUID, size: Int): ByteArray?
}
