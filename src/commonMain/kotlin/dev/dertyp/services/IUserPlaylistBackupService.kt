package dev.dertyp.services

import dev.dertyp.data.UserPlaylistBackup
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Personal playlist backup and restoration for individual users.")
interface IUserPlaylistBackupService {
    @RpcDoc("Create a backup of all playlists owned by the current user.")
    suspend fun createBackup()
    @RpcDoc("List all available playlist backup files for the current user.")
    suspend fun listBackups(): List<BackupInfo>
    @RpcDoc("Restore user playlists from a backup file.")
    suspend fun restoreBackup(
        @RpcParamDoc("Optional name of the backup file. If null, the latest backup is used.") fileName: String? = null
    )
    @RpcDoc("Peek into the contents of a specific user playlist backup.")
    suspend fun getBackupContent(
        @RpcParamDoc("The name of the backup file.") fileName: String
    ): UserPlaylistBackup?
    @RpcDoc("Delete a personal playlist backup file.")
    suspend fun deleteBackup(@RpcParamDoc("The name of the backup file.") fileName: String)
}
