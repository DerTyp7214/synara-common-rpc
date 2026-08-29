package dev.dertyp.ui

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Kinds of app-level events clients can forward to the server.")
enum class UiHookKind {
    @FieldDoc("A URL was shared with the app.") SHARE_URL,
    @FieldDoc("Plain text was shared with the app.") SHARE_TEXT
}

@Serializable
@ModelDoc("An app-level event forwarded by a client.")
sealed class UiHookEvent {
    abstract val kind: UiHookKind

    @Serializable
    @SerialName("shareUrl")
    @ModelDoc("A URL was shared with the app.")
    data class ShareUrl(
        @FieldDoc("The shared URL.")
        val url: String,
        @FieldDoc("Optional title that accompanied the share.")
        val title: String? = null,
    ) : UiHookEvent() {
        override val kind get() = UiHookKind.SHARE_URL
    }

    @Serializable
    @SerialName("shareText")
    @ModelDoc("Plain text was shared with the app.")
    data class ShareText(
        @FieldDoc("The shared text.")
        val text: String,
    ) : UiHookEvent() {
        override val kind get() = UiHookKind.SHARE_TEXT
    }
}

@Serializable
@ModelDoc("A contribution's offer to handle a hook event. The client performs the action directly when it is the only handler, otherwise it lets the user choose.")
data class UiHookHandler(
    @FieldDoc("Id of the offering contribution.")
    val contributionId: String,
    @FieldDoc("Origin: \"server\" or a plugin id.")
    val source: String,
    @FieldDoc("Localized title, e.g. \"Import with Tidal\".")
    val title: String,
    @FieldDoc("Localized description.")
    val description: String? = null,
    @FieldDoc("Icon.")
    val icon: UiIcon? = null,
    @FieldDoc("Action to perform when chosen.")
    val action: UiAction,
    @FieldDoc("If set, ask the user to confirm with this text before performing the action, even when it is the only handler.")
    val confirmText: String? = null,
)
