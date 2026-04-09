package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Instance-to-instance data synchronization.")
interface IRemoteMirrorService {
    @RpcDoc("Get statistics from a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getRemoteStats(@RpcParamDoc("Connection configuration for the remote server.") config: RemoteServerConfig): ServerStats
    @RpcDoc("Start mirroring content from another Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun startMirror(@RpcParamDoc("Mirroring configuration and credentials.") config: RemoteServerConfig)
    @RpcDoc("Stop the active mirroring process.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun stopMirror()
    @RpcDoc("Reset the internal mirroring state.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun resetMirror()
    @RpcDoc("Stream real-time progress updates for the active mirroring task.", adminOnly = true)
    fun getActiveMirrorProgress(): Flow<MirrorProgress>
    @RpcDoc("List all user accounts on a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getRemoteUsers(@RpcParamDoc("Remote connection configuration.") config: RemoteServerConfig): List<User>
    @RpcDoc("List all system playlists on a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getRemotePlaylists(@RpcParamDoc("Remote connection configuration.") config: RemoteServerConfig): List<Playlist>
    @RpcDoc("List all user playlists on a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getRemoteUserPlaylists(@RpcParamDoc("Remote connection configuration.") config: RemoteServerConfig): List<UserPlaylist>
    @RpcDoc("List available proxy instances on a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getProxyInstances(@RpcParamDoc("Remote connection configuration.") config: RemoteServerConfig): List<ProxyInstanceInfo>
    @RpcDoc("Fetch raw image binary data from a remote Synara instance.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getRemoteImageData(
        @RpcParamDoc("Remote connection configuration.") config: RemoteServerConfig,
        @RpcParamDoc("The unique identifier of the image.") imageId: PlatformUUID,
        @RpcParamDoc("Requested image size (width/height).") size: Int
    ): ByteArray?
}
