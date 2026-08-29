@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.ui

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A selectable option of a Select field.")
data class UiOption(
    @FieldDoc("Value submitted when selected.")
    val value: String,
    @FieldDoc("Display label.")
    val label: String,
    @FieldDoc("Optional icon.")
    val icon: UiIcon? = null,
)

@Serializable
@ModelDoc("A row of a Table.")
data class UiTableRow(
    @FieldDoc("Cell texts, one per column.")
    val cells: List<String>,
    @FieldDoc("Action performed when the row is activated.")
    val action: UiAction? = null,
)

@Serializable
@ModelDoc("A node of a server-driven UI tree. Clients render each node with their native design system; unknown nodes are replaced by the server with Fallback according to the client's X-Ui-Schema-Version.")
sealed class UiComponent {
    @Serializable
    @SerialName("column")
    @ModelDoc("Vertical layout container.")
    data class Column(
        @FieldDoc("Children, top to bottom.")
        val children: List<UiComponent>,
        @FieldDoc("Spacing between children.")
        val spacing: UiSpacing = UiSpacing.MEDIUM,
        @FieldDoc("Horizontal alignment of children.")
        val align: UiAlign = UiAlign.STRETCH,
        @FieldDoc("Optional relative weights, one per child.")
        val weights: List<Double>? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("row")
    @ModelDoc("Horizontal layout container.")
    data class Row(
        @FieldDoc("Children, start to end.")
        val children: List<UiComponent>,
        @FieldDoc("Spacing between children.")
        val spacing: UiSpacing = UiSpacing.MEDIUM,
        @FieldDoc("Vertical alignment of children.")
        val align: UiAlign = UiAlign.CENTER,
        @FieldDoc("Optional relative weights, one per child.")
        val weights: List<Double>? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("grid")
    @ModelDoc("Grid layout container.")
    data class Grid(
        @FieldDoc("Children, row-major.")
        val children: List<UiComponent>,
        @FieldDoc("Preferred number of columns; clients may reduce it on narrow screens.")
        val columns: Int = 2,
        @FieldDoc("Spacing between cells.")
        val spacing: UiSpacing = UiSpacing.MEDIUM,
    ) : UiComponent()

    @Serializable
    @SerialName("card")
    @ModelDoc("Elevated container with optional header and trailing actions.")
    data class Card(
        @FieldDoc("Content of the card.")
        val children: List<UiComponent>,
        @FieldDoc("Header title.")
        val title: String? = null,
        @FieldDoc("Header subtitle.")
        val subtitle: String? = null,
        @FieldDoc("Semantic icon name shown in the header.")
        val icon: UiIcon? = null,
        @FieldDoc("Tone of the card.")
        val tone: UiTone = UiTone.DEFAULT,
        @FieldDoc("Action elements (typically Buttons) rendered in the card footer.")
        val actions: List<UiComponent> = emptyList(),
    ) : UiComponent()

    @Serializable
    @SerialName("section")
    @ModelDoc("Titled group of content.")
    data class Section(
        @FieldDoc("Content of the section.")
        val children: List<UiComponent>,
        @FieldDoc("Section title.")
        val title: String,
        @FieldDoc("Whether the user can collapse the section.")
        val collapsible: Boolean = false,
        @FieldDoc("Initial collapsed state.")
        val collapsed: Boolean = false,
    ) : UiComponent()

    @Serializable
    @SerialName("form")
    @ModelDoc("Groups form fields; their values are submitted together with the submit action.")
    data class Form(
        @FieldDoc("Form id referenced by UiAction.Invoke.formId.")
        val id: String,
        @FieldDoc("Content including form fields.")
        val children: List<UiComponent>,
        @FieldDoc("Action dispatched on submit; its formId should equal this form's id.")
        val submit: UiAction.Invoke,
        @FieldDoc("Label of the submit button.")
        val submitLabel: String,
        @FieldDoc("Label of an optional cancel/reset button.")
        val cancelLabel: String? = null,
        @FieldDoc("Extra controls rendered in the trailing row next to (before) the submit button, e.g. a scanner portal.")
        val actions: List<UiComponent> = emptyList(),
    ) : UiComponent()

    @Serializable
    @SerialName("text")
    @ModelDoc("Text.")
    data class Text(
        @FieldDoc("The text.")
        val text: String,
        @FieldDoc("Text style.")
        val style: UiTextStyle = UiTextStyle.BODY,
        @FieldDoc("Tone.")
        val tone: UiTone = UiTone.DEFAULT,
        @FieldDoc("Emphasis.")
        val emphasis: UiEmphasis = UiEmphasis.MEDIUM,
    ) : UiComponent()

    @Serializable
    @SerialName("icon")
    @ModelDoc("A standalone icon.")
    data class Icon(
        @FieldDoc("The icon.")
        val icon: UiIcon,
        @FieldDoc("Tone.")
        val tone: UiTone = UiTone.DEFAULT,
    ) : UiComponent()

    @Serializable
    @SerialName("image")
    @ModelDoc("An image, either from the server image store or an external URL.")
    data class Image(
        @FieldDoc("Id of a server image.")
        val imageId: PlatformUUID? = null,
        @FieldDoc("External image URL, used when imageId is null.")
        val url: String? = null,
        @FieldDoc("Whether to render with rounded corners/circle.")
        val rounded: Boolean = false,
    ) : UiComponent()

    @Serializable
    @SerialName("badge")
    @ModelDoc("Small status label.")
    data class Badge(
        @FieldDoc("Badge text.")
        val text: String,
        @FieldDoc("Tone.")
        val tone: UiTone = UiTone.DEFAULT,
        @FieldDoc("Semantic icon name shown before the text.")
        val icon: UiIcon? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("stat")
    @ModelDoc("A labeled statistic.")
    data class Stat(
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Formatted value.")
        val value: String,
        @FieldDoc("Optional unit suffix.")
        val unit: String? = null,
        @FieldDoc("Semantic icon name.")
        val icon: UiIcon? = null,
        @FieldDoc("Tone.")
        val tone: UiTone = UiTone.DEFAULT,
    ) : UiComponent()

    @Serializable
    @SerialName("progress")
    @ModelDoc("Progress indicator.")
    data class Progress(
        @FieldDoc("Progress from 0.0 to 1.0, or null for indeterminate.")
        val value: Double? = null,
        @FieldDoc("Optional label.")
        val label: String? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("tile")
    @ModelDoc("A tappable tile with title, subtitle and icon.")
    data class Tile(
        @FieldDoc("Title.")
        val title: String,
        @FieldDoc("Subtitle.")
        val subtitle: String? = null,
        @FieldDoc("Semantic icon name.")
        val icon: UiIcon? = null,
        @FieldDoc("Action performed when activated.")
        val action: UiAction? = null,
        @FieldDoc("Tone.")
        val tone: UiTone = UiTone.DEFAULT,
    ) : UiComponent()

    @Serializable
    @SerialName("button")
    @ModelDoc("A button.")
    data class Button(
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Action performed when pressed.")
        val action: UiAction,
        @FieldDoc("Style.")
        val style: UiButtonStyle = UiButtonStyle.SECONDARY,
        @FieldDoc("Semantic icon name.")
        val icon: UiIcon? = null,
        @FieldDoc("Whether the button is enabled.")
        val enabled: Boolean = true,
    ) : UiComponent()

    @Serializable
    @SerialName("listItem")
    @ModelDoc("A list row.")
    data class ListItem(
        @FieldDoc("Title.")
        val title: String,
        @FieldDoc("Subtitle.")
        val subtitle: String? = null,
        @FieldDoc("Semantic icon name.")
        val icon: UiIcon? = null,
        @FieldDoc("Trailing text.")
        val trailing: String? = null,
        @FieldDoc("Action performed when activated.")
        val action: UiAction? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("table")
    @ModelDoc("A simple table.")
    data class Table(
        @FieldDoc("Column headers.")
        val columns: List<String>,
        @FieldDoc("Rows.")
        val rows: List<UiTableRow>,
    ) : UiComponent()

    @Serializable
    @SerialName("spacer")
    @ModelDoc("Empty space.")
    data class Spacer(
        @FieldDoc("Size.")
        val size: UiSpacing = UiSpacing.MEDIUM,
    ) : UiComponent()

    @Serializable
    @SerialName("divider")
    @ModelDoc("A horizontal divider.")
    data object Divider : UiComponent()

    @Serializable
    @SerialName("fallback")
    @ModelDoc("Placeholder the server substitutes for components the client's schema version does not support.")
    data class Fallback(
        @FieldDoc("Optional text to show; clients show a generic message when null.")
        val text: String? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("emptyState")
    @ModelDoc("Placeholder for a screen or section without content: large muted icon, title and description, centered (iOS ContentUnavailableView).")
    data class EmptyState(
        @FieldDoc("Title.")
        val title: String,
        @FieldDoc("Description.")
        val description: String? = null,
        @FieldDoc("Icon.")
        val icon: UiIcon? = null,
        @FieldDoc("Optional action elements (Buttons) below the text.")
        val actions: List<UiComponent> = emptyList(),
    ) : UiComponent()

    @Serializable
    @SerialName("log")
    @ModelDoc("Fixed-height log pane: small monospaced secondary text, one entry per line, scrolled to the newest line. Usually wrapped in Live so lines can be appended via UiLiveUpdate.AppendLines.")
    data class Log(
        @FieldDoc("Current lines, oldest first.")
        val lines: List<String>,
        @FieldDoc("Maximum number of lines to keep; older lines are dropped when appending.")
        val maxLines: Int = 500,
    ) : UiComponent()

    @Serializable
    @SerialName("live")
    @ModelDoc("A subtree that updates independently of the page: render child, then subscribe to IUiService.subscribeLive(contributionId, key) and apply each UiLiveUpdate to it.")
    data class Live(
        @FieldDoc("Key passed to subscribeLive.")
        val key: String,
        @FieldDoc("Initial content.")
        val child: UiComponent,
    ) : UiComponent()

    @Serializable
    @SerialName("native")
    @ModelDoc("Portal the client fills with its own native UI for a known portal name (see UiPortals). Unknown names render the fallback, or nothing.")
    data class Native(
        @FieldDoc("Portal name, e.g. barcodeScanner.")
        val name: String,
        @FieldDoc("Parameters for the native UI, e.g. target = key of the form field a scanned code is appended to.")
        val params: Map<String, String> = emptyMap(),
        @FieldDoc("Rendered when the client does not implement the portal.")
        val fallback: UiComponent? = null,
    ) : UiComponent()

    @Serializable
    @SerialName("textField")
    @ModelDoc("Single- or multi-line text input.")
    data class TextField(
        @FieldDoc("Payload key.")
        val key: String,
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Current value. Never populated for secret fields.")
        val value: String? = null,
        @FieldDoc("Placeholder.")
        val placeholder: String? = null,
        @FieldDoc("Whether input is masked; an empty submitted value means unchanged.")
        val secret: Boolean = false,
        @FieldDoc("Whether the field is multi-line.")
        val multiline: Boolean = false,
        @FieldDoc("Helper text.")
        val helper: String? = null,
        @FieldDoc("Validation error to display.")
        val error: String? = null,
        @FieldDoc("Whether a value is required.")
        val required: Boolean = false,
        @FieldDoc("Whether the field is editable.")
        val enabled: Boolean = true,
        @FieldDoc("Input kind.")
        val kind: UiTextKind = UiTextKind.TEXT,
        @FieldDoc("Keyboard accessory toolbar shown above the on-screen keyboard while the field is focused; items are trailing-aligned. Buttons, Icons and Native portals.")
        val toolbar: List<UiComponent> = emptyList(),
    ) : UiComponent()

    @Serializable
    @SerialName("numberField")
    @ModelDoc("Numeric input.")
    data class NumberField(
        @FieldDoc("Payload key.")
        val key: String,
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Current value.")
        val value: Double? = null,
        @FieldDoc("Minimum value.")
        val min: Double? = null,
        @FieldDoc("Maximum value.")
        val max: Double? = null,
        @FieldDoc("Step; 1.0 for integers.")
        val step: Double? = null,
        @FieldDoc("Helper text.")
        val helper: String? = null,
        @FieldDoc("Validation error to display.")
        val error: String? = null,
        @FieldDoc("Whether a value is required.")
        val required: Boolean = false,
        @FieldDoc("Whether the field is editable.")
        val enabled: Boolean = true,
    ) : UiComponent()

    @Serializable
    @SerialName("switch")
    @ModelDoc("Boolean toggle.")
    data class Switch(
        @FieldDoc("Payload key.")
        val key: String,
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Current value.")
        val value: Boolean = false,
        @FieldDoc("Helper text.")
        val helper: String? = null,
        @FieldDoc("Whether the field is editable.")
        val enabled: Boolean = true,
    ) : UiComponent()

    @Serializable
    @SerialName("select")
    @ModelDoc("Single choice from a list of options.")
    data class Select(
        @FieldDoc("Payload key.")
        val key: String,
        @FieldDoc("Label.")
        val label: String,
        @FieldDoc("Currently selected option value.")
        val value: String? = null,
        @FieldDoc("Available options.")
        val options: List<UiOption> = emptyList(),
        @FieldDoc("Helper text.")
        val helper: String? = null,
        @FieldDoc("Validation error to display.")
        val error: String? = null,
        @FieldDoc("Whether a value is required.")
        val required: Boolean = false,
        @FieldDoc("Whether the field is editable.")
        val enabled: Boolean = true,
    ) : UiComponent()
}
