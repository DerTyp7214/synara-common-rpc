package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Image
import dev.dertyp.data.InsertableImage
import dev.dertyp.rpc.annotations.RestPublic
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Management of image files for covers and profiles.")
interface IImageService {
    @RpcDoc("Get image metadata by its unique ID.")
    suspend fun byId(@RpcParamDoc("The image unique identifier.") id: PlatformUUID): Image?
    @RpcDoc("Find image metadata by its content hash.")
    suspend fun byHash(@RpcParamDoc("The unique hash of the image.") hash: String): Image?
    @RpcDoc("Map a list of image hashes to their existing internal UUIDs.")
    suspend fun getCoverHashes(@RpcParamDoc("Collection of image hashes.") hashes: List<String>): Map<String, PlatformUUID>
    @RestPublic
    @RpcDoc("Retrieve the raw binary data of an image.")
    suspend fun getImageData(
        @RpcParamDoc("The image unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Requested image size (width/height). 0 for original size.") size: Int = 0
    ): ByteArray?
    @RpcDoc("Store a new image on the server.")
    suspend fun createImage(
        @RpcParamDoc("The raw binary data of the image.") bytes: ByteArray,
        @RpcParamDoc("The source or category of the image.") origin: String = "custom"
    ): PlatformUUID

    @RpcDoc("Store multiple images on the server in a single operation.")
    suspend fun createBatch(@RpcParamDoc("Collection of images to store.") images: List<InsertableImage>): Map<String, PlatformUUID>
}
