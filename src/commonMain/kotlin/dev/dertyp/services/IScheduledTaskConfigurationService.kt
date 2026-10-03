package dev.dertyp.services

import dev.dertyp.data.RequiresAdmin
import dev.dertyp.data.TaskConfiguration
import dev.dertyp.rpc.annotations.REMOVED_IN_API_9
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Management of background task configurations.")
interface IScheduledTaskConfigurationService {
    @RequiresAdmin
    @RpcDoc("Retrieve all background task configurations.", errors = ["SecurityException"])
    suspend fun getConfigurations(): List<TaskConfiguration>

    @RequiresAdmin
    @RpcDoc("Update a task configuration.", errors = ["SecurityException", "IllegalArgumentException"])
    suspend fun updateConfiguration(configuration: TaskConfiguration)

    @RequiresAdmin
    @RestGet
    @RpcDoc("Stream real-time updates for all background task configurations.", errors = ["SecurityException"])
    fun observeConfigurations(): Flow<List<TaskConfiguration>>

    @Deprecated(REMOVED_IN_API_9 + " Use observeConfigurations.", ReplaceWith("observeConfigurations()"))
    @RequiresAdmin
    @RpcDoc("Stream real-time updates for all background task configurations.", errors = ["SecurityException"])
    fun getConfigurationsFlow(): Flow<List<TaskConfiguration>> = observeConfigurations()

    @RequiresAdmin
    @RpcDoc("Manually trigger a background task by its unique key.", errors = ["SecurityException"])
    suspend fun triggerTask(key: String): Boolean
}
