package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.SongAudioData
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Service for audio analysis and related data management.")
interface IAudioAnalysisService {
    @RpcDoc("Get audio analysis data for a song.")
    suspend fun getAudioData(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID
    ): SongAudioData?

    @RpcDoc("Trigger analysis for a song.")
    suspend fun analyzeSong(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID
    )
}
