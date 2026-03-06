package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc

@Rpc
interface IDbManagementService {
    suspend fun exportData(): ByteArray
    suspend fun importData(data: ByteArray)
}
