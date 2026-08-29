@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.ui

import dev.dertyp.PlatformUUID
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

object UiSlots {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val ADMIN_DASHBOARD = "admin.dashboard"
    const val HOME = "home"
    const val IMPORTER = "importer"
    const val ALBUM_DETAIL = "album.detail"
    const val ARTIST_DETAIL = "artist.detail"
    const val SONG_DETAIL = "song.detail"
    const val SONG_MENU = "song.menu"
    const val PLAYLIST_DETAIL = "playlist.detail"
}

object UiPortals {
    const val BARCODE_SCANNER = "barcodeScanner"
    const val EXTERNAL_SEARCH = "externalSearch"
}

@Serializable
@ModelDoc("Describes a UI contribution available to the current user.")
data class UiContributionInfo(
    @FieldDoc("Unique id, matching [a-z0-9._-]+.")
    val id: String,
    @FieldDoc("Origin: \"server\" or a plugin id.")
    val source: String,
    @FieldDoc("Kind of contribution.")
    val kind: UiContributionKind,
    @FieldDoc("Slot name for SLOT contributions.")
    val slot: String? = null,
    @FieldDoc("Localized title.")
    val title: String,
    @FieldDoc("Localized description.")
    val description: String? = null,
    @FieldDoc("Semantic icon name.")
    val icon: String? = null,
    @FieldDoc("Sort order within a slot; lower first.")
    val order: Int = 0,
    @FieldDoc("Whether subscribe() emits updates after the initial render.")
    val live: Boolean = false,
    @FieldDoc("Preferred card size for HOME_CARD contributions.")
    val cardSize: UiCardSize = UiCardSize.MEDIUM,
    @FieldDoc("Whether the contribution is restricted to admins.")
    val requiresAdmin: Boolean = false,
    @FieldDoc("User capabilities required to see and use the contribution.")
    val requiredCapabilities: List<UserCapability> = emptyList(),
    @FieldDoc("App hooks the contribution offers to handle, see IUiService.dispatchHook.")
    val hooks: List<UiHookKind> = emptyList(),
)

@Serializable
@ModelDoc("Context in which a contribution is rendered, e.g. the entity whose detail screen hosts the slot.")
data class UiContext(
    @FieldDoc("Type of the host entity, if any.")
    val entityType: UiEntityType? = null,
    @FieldDoc("Id of the host entity, if any.")
    val entityId: PlatformUUID? = null,
    @FieldDoc("Free-form parameters, e.g. from UiAction.OpenPage.")
    val params: Map<String, String> = emptyMap(),
)

@Serializable
@ModelDoc("A rendered contribution.")
data class UiRender(
    @FieldDoc("Id of the contribution.")
    val contributionId: String,
    @FieldDoc("Root of the component tree.")
    val root: UiComponent,
    @FieldDoc("Localized title, e.g. for a page header.")
    val title: String? = null,
    @FieldDoc("UI schema version the tree was shaped for.")
    val schemaVersion: Int = UiSchemaVersion.CURRENT,
    @FieldDoc("Monotonic revision, increases with each re-render.")
    val revision: Long = 0,
    @FieldDoc("Elements for the native toolbar/app bar of a page (Buttons, Icons, Native portals). Empty for slot items.")
    val toolbar: List<UiComponent> = emptyList(),
)

@Serializable
@ModelDoc("All contributions rendered for a slot.")
data class UiSlotRender(
    @FieldDoc("Slot name.")
    val slot: String,
    @FieldDoc("Rendered contributions in display order.")
    val items: List<UiRender>,
)

@Serializable
@ModelDoc("Payload of an action invocation.")
data class UiInvokePayload(
    @FieldDoc("Form field values keyed by field key, merged with the action's static params.")
    val values: Map<String, UiValue> = emptyMap(),
    @FieldDoc("Context the element was rendered in.")
    val context: UiContext = UiContext(),
)

@Serializable
@ModelDoc("Result of an action invocation.")
data class UiInvokeResult(
    @FieldDoc("Outcome.")
    val status: UiInvokeStatus,
    @FieldDoc("Localized message to show the user.")
    val message: String? = null,
    @FieldDoc("Per-field validation errors keyed by field key.")
    val fieldErrors: Map<String, String> = emptyMap(),
    @FieldDoc("Whether the client should re-render the contribution.")
    val refresh: Boolean = false,
    @FieldDoc("Follow-up action the client should perform.")
    val next: UiAction? = null,
)

@Serializable
@ModelDoc("A home-screen card and its pin state for the current user.")
data class UiHomeCard(
    @FieldDoc("Id of the HOME_CARD contribution.")
    val contributionId: String,
    @FieldDoc("Whether the user pinned the card.")
    val pinned: Boolean,
    @FieldDoc("Position among pinned cards.")
    val position: Int,
    @FieldDoc("Preferred card size.")
    val size: UiCardSize,
)

@Serializable
@ModelDoc("The current user's home-screen card layout, including unpinned cards available for pinning.")
data class UiHomeLayout(
    @FieldDoc("Cards, pinned ones first in position order.")
    val cards: List<UiHomeCard>,
)
