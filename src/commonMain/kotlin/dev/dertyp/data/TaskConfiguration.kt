package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("A serializable definition of a task trigger.")
sealed class TriggerDefinition {
    @Serializable
    @ModelDoc("A trigger based on a cron expression.")
    data class Cron(
        @FieldDoc("The cron expression (UNIX style).")
        val expression: String
    ) : TriggerDefinition()

    @Serializable
    @ModelDoc("A trigger that executes at a fixed interval.")
    data class Interval(
        @FieldDoc("The interval in seconds.")
        val intervalSeconds: Long
    ) : TriggerDefinition()

    @Serializable
    @ModelDoc("A trigger that executes after another task completes.")
    data class AfterTask(
        @FieldDoc("The unique key of the dependency task.")
        val dependencyKey: String
    ) : TriggerDefinition()

    @Serializable
    @ModelDoc("A trigger that only executes when manually requested.")
    data object Manual : TriggerDefinition()
}

@Serializable
@ModelDoc("Configuration for a background scheduled task.")
data class TaskConfiguration(
    @FieldDoc("The unique key identifying the task.")
    val key: String,
    @FieldDoc("A human-readable name for the task.")
    val name: String,
    @FieldDoc("Whether the task is enabled and will be scheduled.")
    val enabled: Boolean,
    @FieldDoc("The trigger definition for the task.")
    val trigger: TriggerDefinition
)
