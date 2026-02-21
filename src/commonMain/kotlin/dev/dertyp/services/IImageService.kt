package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Image
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IImageService {
    suspend fun byId(id: PlatformUUID): Image?
    suspend fun byHash(hash: String): Image?
    suspend fun getCoverHashes(hashes: List<String>): Map<String, PlatformUUID>
    suspend fun getImageData(id: PlatformUUID, size: Int = 0): ByteArray?
    suspend fun createImage(bytes: ByteArray, origin: String = "custom"): PlatformUUID
}