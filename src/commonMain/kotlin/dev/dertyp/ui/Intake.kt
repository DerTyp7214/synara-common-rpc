@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.ui

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.services.import.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Kind of a catalog code handed to the intake.")
enum class UiIntakeCodeKind {
    @FieldDoc("International Standard Recording Code (a track).") ISRC,
    @FieldDoc("UPC/EAN/GTIN barcode (a release).") UPC
}

@Serializable
@ModelDoc("Something a user hands to the server to act on: a link, a catalog code, a provider id, free text or a file. Plugins offer to handle items through intake resolvers.")
sealed class IntakeItem {
    @Serializable
    @SerialName("url")
    @ModelDoc("A link.")
    data class Url(
        @FieldDoc("The URL.")
        val url: String,
    ) : IntakeItem()

    @Serializable
    @SerialName("code")
    @ModelDoc("A catalog code.")
    data class Code(
        @FieldDoc("Code kind.")
        val kind: UiIntakeCodeKind,
        @FieldDoc("Normalized code (uppercase, no separators).")
        val value: String,
    ) : IntakeItem()

    @Serializable
    @SerialName("id")
    @ModelDoc("An id in a provider's namespace, e.g. provider \"tidal\", id \"123\".")
    data class Id(
        @FieldDoc("Provider / importer id; empty means the server default.")
        val provider: String,
        @FieldDoc("The id.")
        val id: String,
        @FieldDoc("Content type if known.")
        val contentType: Type? = null,
    ) : IntakeItem()

    @Serializable
    @SerialName("text")
    @ModelDoc("Free text, e.g. a search query.")
    data class Text(
        @FieldDoc("The text.")
        val text: String,
    ) : IntakeItem()

    @Serializable
    @SerialName("file")
    @ModelDoc("A file previously uploaded to the server.")
    data class File(
        @FieldDoc("Server file id.")
        val fileId: PlatformUUID,
        @FieldDoc("Original file name.")
        val name: String,
        @FieldDoc("MIME type if known.")
        val mimeType: String? = null,
    ) : IntakeItem()

    companion object {
        private val ISRC = Regex("^[A-Z]{2}[A-Z0-9]{3}[0-9]{7}$")
        private val UPC_LENGTHS = setOf(8, 12, 13, 14)
        private val PREFIXED_ID = Regex("^([a-z][a-z0-9_-]*):(\\S+)$")

        fun parse(line: String): IntakeItem {
            val trimmed = line.trim()
            if (trimmed.contains("://")) return Url(trimmed)
            val compact = trimmed.replace("-", "").replace(" ", "")
            if (ISRC.matches(compact.uppercase())) return Code(UiIntakeCodeKind.ISRC, compact.uppercase())
            if (compact.length in UPC_LENGTHS && compact.all { it.isDigit() }) return Code(UiIntakeCodeKind.UPC, compact)
            PREFIXED_ID.matchEntire(trimmed)?.let { return Id(it.groupValues[1], it.groupValues[2]) }
            return Text(trimmed)
        }

        fun parseLines(text: String): List<IntakeItem> =
            text.lines().map { it.trim() }.filter { it.isNotEmpty() }.map(::parse)
    }
}

@Serializable
@ModelDoc("Outcome of IUiService.intake.")
enum class UiIntakeStatus {
    @FieldDoc("Everything acceptable was submitted.") OK,
    @FieldDoc("Several handlers offer; let the user pick one of handlers and call intake again with its action.") NEEDS_CHOICE,
    @FieldDoc("No handler accepted any item; fall back to native behaviour.") UNHANDLED,
    @FieldDoc("The user may not use the offering handlers.") UNAUTHORIZED,
    @FieldDoc("Submission failed; see message.") ERROR
}

@Serializable
@ModelDoc("Result of handing items to the intake.")
data class UiIntakeResult(
    @FieldDoc("Outcome.")
    val status: UiIntakeStatus,
    @FieldDoc("Localized message to show the user.")
    val message: String? = null,
    @FieldDoc("Number of items that were submitted.")
    val accepted: Int = 0,
    @FieldDoc("Items no handler accepted.")
    val rejected: List<IntakeItem> = emptyList(),
    @FieldDoc("Handlers to choose from when status is NEEDS_CHOICE (each action is an Intake action with the handler preselected) or navigational alternatives.")
    val handlers: List<UiHookHandler> = emptyList(),
    @FieldDoc("Follow-up action the client should perform, e.g. open the queue.")
    val next: UiAction? = null,
)
