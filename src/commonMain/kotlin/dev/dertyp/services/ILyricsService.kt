package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.services.models.SyncedLyrics
import kotlinx.rpc.annotations.Rpc

@Rpc
interface ILyricsService {
    suspend fun getSyncedLyrics(songId: PlatformUUID): SyncedLyrics?
    suspend fun transcribeLyrics(songId: PlatformUUID, lyrics: String? = null): SyncedLyrics?
    suspend fun startSyncWorker(): Boolean
    suspend fun isConfigured(): Boolean
}
