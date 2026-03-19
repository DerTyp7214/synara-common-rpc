package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc

@Rpc
interface IUserPlaylistBackupService {
    suspend fun createBackup()
    suspend fun listBackups(): List<BackupInfo>
    suspend fun restoreBackup(fileName: String? = null)
}
