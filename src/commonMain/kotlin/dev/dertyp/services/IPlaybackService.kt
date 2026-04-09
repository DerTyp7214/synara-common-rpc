package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PlaybackState
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Synchronize music playback status across multiple devices.")
interface IPlaybackService {
    @RpcDoc("Retrieve the current playback state for a specific session.")
    suspend fun getPlaybackState(@RpcParamDoc("The session unique identifier.") sessionId: PlatformUUID): PlaybackState?
    @RpcDoc("Update the playback state for a specific session.")
    suspend fun setPlaybackState(
        @RpcParamDoc("The session unique identifier.") sessionId: PlatformUUID,
        @RpcParamDoc("The new playback state data.") state: PlaybackState
    ): Boolean
    @RpcDoc("Watch real-time playback state changes for a session.")
    fun observePlaybackState(@RpcParamDoc("The session unique identifier.") sessionId: PlatformUUID): Flow<PlaybackState>
}
