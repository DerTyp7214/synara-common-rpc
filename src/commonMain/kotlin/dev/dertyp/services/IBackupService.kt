package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
data class BackupInfo(
    val name: String,
    val size: Long,
    val date: Long
)

@Rpc
interface IBackupService {
    suspend fun listBackups(): List<BackupInfo>
    suspend fun loadBackup(fileName: String)
    suspend fun deleteBackup(fileName: String)
    suspend fun createBackup()
}
