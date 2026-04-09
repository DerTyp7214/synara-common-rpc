package dev.dertyp.services

import dev.dertyp.data.ScheduledTaskLog
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Monitoring and tracking of background scheduled tasks.")
interface IScheduledTaskLogService {
    @RpcDoc("Retrieve a snapshot of the most recent background task logs grouped by task name.", adminOnly = true, errors = ["SecurityException"])
    suspend fun getGroupedLogs(): Map<String, List<ScheduledTaskLog>>
    @RpcDoc("Stream real-time updates for all background task progress and completion.", adminOnly = true, errors = ["SecurityException"])
    fun getGroupedLogsFlow(): Flow<Map<String, List<ScheduledTaskLog>>>
}
