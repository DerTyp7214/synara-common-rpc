package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.services.models.SyncedLyrics
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Syncing and management of track lyrics.")
interface ILyricsService {
    @RpcDoc("Get time-synced lyrics for a song if they exist.")
    suspend fun getSyncedLyrics(@RpcParamDoc("The song unique identifier.") songId: PlatformUUID): SyncedLyrics?
    @RpcDoc("Trigger AI-based transcription or manual alignment of lyrics.", errors = ["RuntimeException"])
    suspend fun transcribeLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("Optional raw lyrics text to transcribe.") lyrics: String? = null
    ): SyncedLyrics?
    @RpcDoc("Start the background lyrics synchronization worker.")
    suspend fun startSyncWorker(): Boolean
}
