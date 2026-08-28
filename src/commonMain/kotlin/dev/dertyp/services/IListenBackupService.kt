package dev.dertyp.services

import dev.dertyp.data.ListenBackupConfig
import dev.dertyp.data.ListenBackupConnectionTest
import dev.dertyp.data.ListenBackupState
import dev.dertyp.data.RequiresAdmin
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Configure and control the backup of server-side listens to a remote listen-backup receiver.")
interface IListenBackupService {
    @RequiresAdmin
    @RestGet
    @RpcDoc("Get the current backup configuration and sync state.", errors = ["SecurityException"])
    suspend fun getState(): ListenBackupState

    @RequiresAdmin
    @RpcDoc("Stream the backup state, re-emitting whenever the configuration or sync progress changes.", errors = ["SecurityException"])
    fun getStateFlow(): Flow<ListenBackupState>

    @RequiresAdmin
    @RestPost
    @RpcDoc("Update the backup configuration.", errors = ["SecurityException", "IllegalArgumentException"])
    suspend fun updateConfig(
        @RpcParamDoc("The new configuration. A null key keeps the stored key.") config: ListenBackupConfig
    ): ListenBackupState

    @RequiresAdmin
    @RestPost
    @RpcDoc("Probe a receiver. Uses the stored configuration when no config is given.", errors = ["SecurityException"])
    suspend fun testConnection(
        @RpcParamDoc("Configuration to probe; null uses the stored configuration.") config: ListenBackupConfig? = null
    ): ListenBackupConnectionTest

    @RequiresAdmin
    @RestPost
    @RpcDoc("Run a backup sync now and return the resulting state.", errors = ["SecurityException", "IllegalStateException"])
    suspend fun syncNow(): ListenBackupState

    @RequiresAdmin
    @RestPost
    @RpcDoc("Reset the sync cursor so the next run re-pushes every local listen.", errors = ["SecurityException"])
    suspend fun resetCursor(): ListenBackupState
}
