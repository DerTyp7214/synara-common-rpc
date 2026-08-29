package dev.dertyp.ui

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("An update for a Live subtree, streamed by IUiService.subscribeLive.")
sealed class UiLiveUpdate {
    @Serializable
    @SerialName("replace")
    @ModelDoc("Replace the Live child with a new component.")
    data class Replace(
        @FieldDoc("The new child.")
        val child: UiComponent,
    ) : UiLiveUpdate()

    @Serializable
    @SerialName("appendLines")
    @ModelDoc("Append lines to a Log child, dropping the oldest beyond its maxLines.")
    data class AppendLines(
        @FieldDoc("Lines to append, oldest first.")
        val lines: List<String>,
    ) : UiLiveUpdate()
}
