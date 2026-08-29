@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.ui

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("An entry of a menu opened by UiAction.OpenMenu.")
data class UiMenuItem(
    @FieldDoc("Localized label.")
    val label: String,
    @FieldDoc("Action performed when chosen; may itself be OpenMenu for a sub-menu.")
    val action: UiAction,
    @FieldDoc("Icon.")
    val icon: UiIcon? = null,
    @FieldDoc("Tone, e.g. ERROR for destructive entries.")
    val tone: UiTone = UiTone.DEFAULT,
    @FieldDoc("Whether the entry can be chosen.")
    val enabled: Boolean = true,
)

@Serializable
@ModelDoc("An action a client performs when the user interacts with a UI element.")
sealed class UiAction {
    @Serializable
    @SerialName("openMenu")
    @ModelDoc("Show a native menu (dropdown, context menu or bottom sheet) with the given entries, anchored to the element.")
    data class OpenMenu(
        @FieldDoc("Menu entries in order.")
        val items: List<UiMenuItem>,
        @FieldDoc("Optional menu title.")
        val title: String? = null,
    ) : UiAction()

    @Serializable
    @SerialName("invoke")
    @ModelDoc("Dispatch an action to the contribution via IUiService.invoke.")
    data class Invoke(
        @FieldDoc("Contribution that handles the action.")
        val contributionId: String,
        @FieldDoc("Action identifier understood by the contribution.")
        val actionId: String,
        @FieldDoc("Static parameters sent with the action.")
        val params: Map<String, UiValue> = emptyMap(),
        @FieldDoc("If set, the current values of the Form with this id are sent as the payload values.")
        val formId: String? = null,
        @FieldDoc("If set, the client asks the user to confirm with this text before dispatching.")
        val confirmText: String? = null,
    ) : UiAction()

    @Serializable
    @SerialName("openEntity")
    @ModelDoc("Navigate to a library entity using the client's native screen.")
    data class OpenEntity(
        @FieldDoc("Type of the entity.")
        val entityType: UiEntityType,
        @FieldDoc("Id of the entity.")
        val entityId: PlatformUUID,
    ) : UiAction()

    @Serializable
    @SerialName("openPage")
    @ModelDoc("Navigate to a server-driven page contribution.")
    data class OpenPage(
        @FieldDoc("Id of the PAGE contribution.")
        val pageId: String,
        @FieldDoc("Parameters passed to the page as UiContext.params.")
        val params: Map<String, String> = emptyMap(),
        @FieldDoc("Present the page modally (sheet/dialog) instead of pushing it.")
        val modal: Boolean = false,
    ) : UiAction()

    @Serializable
    @SerialName("dismissKeyboard")
    @ModelDoc("Unfocus the current input and close the on-screen keyboard; no-op where there is none.")
    data object DismissKeyboard : UiAction()

    @Serializable
    @SerialName("openUrl")
    @ModelDoc("Open an external URL.")
    data class OpenUrl(
        @FieldDoc("The URL to open.")
        val url: String,
    ) : UiAction()

    @Serializable
    @SerialName("openNative")
    @ModelDoc("Open a native client screen identified by a portal name, see UiPortals.")
    data class OpenNative(
        @FieldDoc("Portal name, e.g. externalSearch.")
        val name: String,
        @FieldDoc("Parameters for the native screen, e.g. query.")
        val params: Map<String, String> = emptyMap(),
    ) : UiAction()

    @Serializable
    @SerialName("refresh")
    @ModelDoc("Re-render the contribution the element belongs to.")
    data object Refresh : UiAction()
}
