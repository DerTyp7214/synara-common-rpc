package dev.dertyp.services

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Metadata for a specific system backup file.")
data class BackupInfo(
    @FieldDoc("The name of the backup file.")
    val name: String,
    @FieldDoc("Size of the backup in bytes.")
    val size: Long,
    @FieldDoc("Creation date as a Unix timestamp.")
    val date: Long
)

@Serializable
@ModelDoc("Results of a successful system backup creation.")
data class BackupResult(
    @FieldDoc("The name of the newly created file.")
    val fileName: String,
    @FieldDoc("Final size of the backup in bytes.")
    val size: Long,
    @FieldDoc("Number of cover images included in the backup.")
    val imageCount: Int
)

@Rpc
@RpcDoc("System-wide data persistence and disaster recovery.")
interface IBackupService {
    @RpcDoc("List all available system backup files.", adminOnly = true, errors = ["SecurityException"])
    suspend fun listBackups(): List<BackupInfo>
    @RpcDoc("Restore the entire server state from a backup file.", adminOnly = true, errors = ["SecurityException", "IllegalArgumentException"])
    suspend fun loadBackup(@RpcParamDoc("The name of the backup file.") fileName: String)
    @RpcDoc("Delete a system backup file from the server.", adminOnly = true, errors = ["SecurityException"])
    suspend fun deleteBackup(@RpcParamDoc("The name of the backup file.") fileName: String)
    @RpcDoc("Trigger the creation of a full system backup.", adminOnly = true, errors = ["SecurityException"])
    suspend fun createBackup(): BackupResult
}
