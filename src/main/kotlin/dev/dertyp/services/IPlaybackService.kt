package dev.dertyp.services

import dev.dertyp.data.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import java.util.UUID

@Rpc
interface IPlaybackService {
    suspend fun getPlaybackState(sessionId: UUID): PlaybackState?
    suspend fun setPlaybackState(sessionId: UUID, state: PlaybackState): Boolean
    fun observePlaybackState(sessionId: UUID): Flow<PlaybackState>
}
