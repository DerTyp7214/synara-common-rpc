package dev.dertyp.services

import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Monitoring of physical disk usage for the media library.")
interface IStorageService {
    @RpcDoc("Calculate and return the total storage space consumed by the music library in bytes.")
    suspend fun getTotalStorage(): Long
}