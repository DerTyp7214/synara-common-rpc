package dev.dertyp.services

import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Direct database management and data migration.")
interface IDbManagementService {
    @RestGet
    @RpcDoc("Export the entire system database as a binary blob.")
    suspend fun exportData(): ByteArray
    @RpcDoc("Import a previously exported database blob to overwrite the current state.")
    suspend fun importData(@RpcParamDoc("The raw database blob.") data: ByteArray)
}
