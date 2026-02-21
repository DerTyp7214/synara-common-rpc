package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.CustomMetadata
import kotlinx.rpc.annotations.Rpc

@Rpc
interface ICustomAudioService {
    suspend fun uploadCustomAudio(
        fileData: ByteArray,
        fileName: String,
        metadata: CustomMetadata? = null
    ): PlatformUUID?
}
