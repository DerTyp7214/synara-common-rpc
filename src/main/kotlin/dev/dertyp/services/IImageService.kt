package dev.dertyp.services

import dev.dertyp.data.Image
import kotlinx.rpc.annotations.Rpc
import java.util.*

@Rpc
interface IImageService {
    suspend fun byId(id: UUID): Image?
    suspend fun byHash(hash: String): Image?
    suspend fun getCoverHashes(hashes: List<String>): Map<String, UUID>
    suspend fun getImageData(id: UUID, size: Int = 0): ByteArray?
    suspend fun createImage(bytes: ByteArray, origin: String = "custom"): UUID
}