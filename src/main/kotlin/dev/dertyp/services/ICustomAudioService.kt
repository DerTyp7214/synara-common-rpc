package dev.dertyp.services

import dev.dertyp.data.CustomMetadata
import kotlinx.rpc.annotations.Rpc
import java.util.*

@Rpc
interface ICustomAudioService {
    suspend fun uploadCustomAudio(
        fileData: ByteArray,
        fileName: String,
        metadata: CustomMetadata? = null
    ): UUID?
}
