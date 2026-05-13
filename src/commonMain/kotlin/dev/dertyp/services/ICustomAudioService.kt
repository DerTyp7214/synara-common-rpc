package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.CustomMetadata
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Handles manual audio uploads.")
interface ICustomAudioService {
    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Upload a custom audio file and its metadata.")
    suspend fun uploadCustomAudio(
        @RpcParamDoc("The raw bytes of the audio file.") fileData: ByteArray,
        @RpcParamDoc("The original name of the file.") fileName: String,
        @RpcParamDoc("Optional metadata associated with the audio.") metadata: CustomMetadata? = null
    ): PlatformUUID?
}
