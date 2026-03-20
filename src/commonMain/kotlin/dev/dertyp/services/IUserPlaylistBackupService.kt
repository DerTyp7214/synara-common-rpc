package dev.dertyp.services

import dev.dertyp.data.UserPlaylist
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IUserPlaylistBackupService {
    suspend fun createBackup()
    suspend fun listBackups(): List<BackupInfo>
    suspend fun restoreBackup(fileName: String? = null)
    suspend fun getBackupContent(fileName: String): List<UserPlaylist>
    suspend fun deleteBackup(fileName: String)
}
