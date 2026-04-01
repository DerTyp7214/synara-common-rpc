@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
enum class TaskStatus { SUCCESS, FAILURE, RUNNING }

@Serializable
data class ScheduledTaskLog(
    val id: PlatformUUID,
    val taskName: String,
    val startTime: Long,
    val endTime: Long,
    val status: TaskStatus,
    val message: String?,
    val details: Map<String, String>?,
    val progress: Double = 0.0,
    val logs: List<String> = emptyList(),
    val logTime: Long
)
