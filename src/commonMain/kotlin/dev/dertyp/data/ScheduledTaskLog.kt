@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("The status of a background scheduled task.")
enum class TaskStatus {
    @FieldDoc("The task completed successfully.") SUCCESS,
    @FieldDoc("The task encountered an error.") FAILURE,
    @FieldDoc("The task is currently in progress.") RUNNING
}

@Serializable
@ModelDoc("Detailed log entry for a background scheduled task execution.")
data class ScheduledTaskLog(
    @FieldDoc("The unique identifier of the log entry.")
    val id: PlatformUUID,
    @FieldDoc("The internal name of the task.")
    val taskName: String,
    @FieldDoc("Unix timestamp of when the task started.")
    val startTime: Long,
    @FieldDoc("Unix timestamp of when the task ended.")
    val endTime: Long,
    @FieldDoc("The current or final status of the task.")
    val status: TaskStatus,
    @FieldDoc("Summary message about the task execution.")
    val message: String?,
    @FieldDoc("Key-value pairs containing task-specific details.")
    val details: Map<String, String>?,
    @FieldDoc("Progress of the task as a fraction (0.0 to 1.0).")
    val progress: Double = 0.0,
    @FieldDoc("Collection of log lines produced by the task.")
    val logs: List<String> = emptyList(),
    @FieldDoc("Unix timestamp of the log entry.")
    val logTime: Long
)
