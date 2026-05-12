package dev.dertyp.services

import dev.dertyp.data.RequiresAdmin
import dev.dertyp.data.ScheduledTaskLog
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Monitoring and tracking of background scheduled tasks.")
interface IScheduledTaskLogService {
    @RequiresAdmin
    @RpcDoc("Retrieve a snapshot of the most recent background task logs grouped by task name.", errors = ["SecurityException"])
    suspend fun getGroupedLogs(): Map<String, List<ScheduledTaskLog>>
    @RequiresAdmin
    @RpcDoc("Stream real-time updates for all background task progress and completion.", errors = ["SecurityException"])
    fun getGroupedLogsFlow(): Flow<Map<String, List<ScheduledTaskLog>>>
}
