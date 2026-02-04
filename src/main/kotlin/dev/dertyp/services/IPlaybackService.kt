package dev.dertyp.services

import dev.dertyp.data.PlaybackState
import kotlinx.rpc.annotations.Rpc
import java.util.UUID

@Rpc
interface IPlaybackService {
    suspend fun getPlaybackState(sessionId: UUID): PlaybackState?
    suspend fun setPlaybackState(sessionId: UUID, state: PlaybackState): Boolean
}
