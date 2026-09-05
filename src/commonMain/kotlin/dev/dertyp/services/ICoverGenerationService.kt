@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.CoverGenerationOptions
import dev.dertyp.data.CoverGenerationParams
import dev.dertyp.data.CoverInfo
import dev.dertyp.data.CoverTarget
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Generates cover images for user playlists and collections from their content, asset packs and palette.")
interface ICoverGenerationService {
    @RestGet
    @RpcDoc("Styles and asset packs available to the current user.")
    suspend fun options(): CoverGenerationOptions

    @RpcDoc("The current cover state of a playlist or collection.", errors = ["IllegalArgumentException"])
    suspend fun coverInfo(@RpcParamDoc("The playlist or collection.") target: CoverTarget): CoverInfo

    @RpcDoc("Render a 1024x1024 JPEG preview without persisting it. Only the owner or an admin may call this.", errors = ["IllegalArgumentException", "IllegalAccessException"])
    suspend fun previewCoverImage(
        @RpcParamDoc("The playlist or collection.") target: CoverTarget,
        @RpcParamDoc("Generation parameters.") params: CoverGenerationParams = CoverGenerationParams(),
    ): ByteArray

    @RestPost
    @RpcDoc("Render, persist and set the cover, marking it as generated. Returns the new image id. Only the owner or an admin may call this.", errors = ["IllegalArgumentException", "IllegalAccessException"])
    suspend fun applyCover(
        @RpcParamDoc("The playlist or collection.") target: CoverTarget,
        @RpcParamDoc("Generation parameters.") params: CoverGenerationParams = CoverGenerationParams(),
    ): PlatformUUID

    @RestPost
    @RpcDoc("Clear a user-set cover so automatic generation may replace it. Returns false when there was nothing to reset.", errors = ["IllegalAccessException"])
    suspend fun resetCover(@RpcParamDoc("The playlist or collection.") target: CoverTarget): Boolean

    @RestPost
    @RpcDoc("Queue generation for every playlist and collection of the current user without a cover. Returns the job id.")
    suspend fun generateMissing(
        @RpcParamDoc("Generation parameters applied to every item.") params: CoverGenerationParams = CoverGenerationParams(),
    ): PlatformUUID
}
