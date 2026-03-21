package dev.dertyp.services

import dev.dertyp.data.ScheduledTaskLog
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IScheduledTaskLogService {
    suspend fun getGroupedLogs(): Map<String, List<ScheduledTaskLog>>
}
