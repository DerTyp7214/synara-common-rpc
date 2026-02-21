package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IPlaybackService {
    suspend fun getPlaybackState(sessionId: PlatformUUID): PlaybackState?
    suspend fun setPlaybackState(sessionId: PlatformUUID, state: PlaybackState): Boolean
    fun observePlaybackState(sessionId: PlatformUUID): Flow<PlaybackState>
}
