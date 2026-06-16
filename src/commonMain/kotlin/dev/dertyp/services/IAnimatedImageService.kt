@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.AnimatedImage
import dev.dertyp.data.InsertableAnimatedImage
import dev.dertyp.rpc.annotations.RestPublic
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Management of animated cover files (short looping videos).")
interface IAnimatedImageService {
    @RpcDoc("Get animated image metadata by its unique ID.")
    suspend fun byId(@RpcParamDoc("The animated image unique identifier.") id: PlatformUUID): AnimatedImage?
    @RpcDoc("Find animated image metadata by its content hash.")
    suspend fun byHash(@RpcParamDoc("The unique hash of the animated image.") hash: String): AnimatedImage?
    @RpcDoc("Map a list of animated image hashes to their existing internal UUIDs.")
    suspend fun getCoverHashes(@RpcParamDoc("Collection of animated image hashes.") hashes: List<String>): Map<String, PlatformUUID>
    @RestPublic
    @RpcDoc("Retrieve the raw binary data of an animated image.")
    suspend fun getAnimatedImageData(
        @RpcParamDoc("The animated image unique identifier.") id: PlatformUUID
    ): ByteArray?
    @RpcDoc("Store a new animated image on the server.")
    suspend fun createAnimatedImage(
        @RpcParamDoc("The raw binary data of the animated image.") bytes: ByteArray,
        @RpcParamDoc("The source or category of the animated image.") origin: String = "custom"
    ): PlatformUUID

    @RpcDoc("Store multiple animated images on the server in a single operation.")
    suspend fun createBatch(@RpcParamDoc("Collection of animated images to store.") images: List<InsertableAnimatedImage>): Map<String, PlatformUUID>
}
