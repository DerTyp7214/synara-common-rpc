package dev.dertyp.ui

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Semantic color tone of a UI element. Clients map tones to their own design system.")
enum class UiTone {
    @FieldDoc("Neutral default appearance.") DEFAULT,
    @FieldDoc("Accent/brand color.") PRIMARY,
    @FieldDoc("Positive state.") SUCCESS,
    @FieldDoc("Warning state.") WARNING,
    @FieldDoc("Error/destructive state.") ERROR,
    @FieldDoc("De-emphasized/secondary appearance.") MUTED
}

@Serializable
@ModelDoc("Visual emphasis of a UI element.")
enum class UiEmphasis {
    @FieldDoc("Subtle.") LOW,
    @FieldDoc("Regular.") MEDIUM,
    @FieldDoc("Prominent.") HIGH
}

@Serializable
@ModelDoc("Relative spacing between children or around an element.")
enum class UiSpacing {
    @FieldDoc("No spacing.") NONE,
    @FieldDoc("Small spacing.") SMALL,
    @FieldDoc("Medium spacing.") MEDIUM,
    @FieldDoc("Large spacing.") LARGE
}

@Serializable
@ModelDoc("Cross-axis alignment of children inside a layout container.")
enum class UiAlign {
    @FieldDoc("Align to the start.") START,
    @FieldDoc("Center.") CENTER,
    @FieldDoc("Align to the end.") END,
    @FieldDoc("Stretch to fill.") STRETCH,
    @FieldDoc("Distribute with space between children.") SPACE_BETWEEN
}

@Serializable
@ModelDoc("Semantic text style.")
enum class UiTextStyle {
    @FieldDoc("Large heading.") TITLE,
    @FieldDoc("Secondary heading.") SUBTITLE,
    @FieldDoc("Regular body text.") BODY,
    @FieldDoc("Small helper text.") CAPTION,
    @FieldDoc("Monospaced text.") CODE
}

@Serializable
@ModelDoc("Semantic button style.")
enum class UiButtonStyle {
    @FieldDoc("Filled accent button.") PRIMARY,
    @FieldDoc("Outlined/tonal button.") SECONDARY,
    @FieldDoc("Destructive action button.") DESTRUCTIVE,
    @FieldDoc("Text-only button.") TEXT
}

@Serializable
@ModelDoc("Preferred size of a home-screen card.")
enum class UiCardSize {
    @FieldDoc("Compact card.") SMALL,
    @FieldDoc("Regular card.") MEDIUM,
    @FieldDoc("Tall card.") LARGE,
    @FieldDoc("Full-width card.") WIDE
}

@Serializable
@ModelDoc("Kind of a UI contribution.")
enum class UiContributionKind {
    @FieldDoc("Rendered inside a named slot of a native screen.") SLOT,
    @FieldDoc("A standalone page owned by the contribution.") PAGE,
    @FieldDoc("A pinnable card on the home screen.") HOME_CARD
}

@Serializable
@ModelDoc("Type of a library entity a UI element refers to.")
enum class UiEntityType {
    @FieldDoc("A song.") SONG,
    @FieldDoc("An album.") ALBUM,
    @FieldDoc("An artist.") ARTIST,
    @FieldDoc("A playlist.") PLAYLIST,
    @FieldDoc("A user.") USER
}

@Serializable
@ModelDoc("Input kind of a TextField; lets clients pick a keyboard or offer a scanner.")
enum class UiTextKind {
    @FieldDoc("Plain text.") TEXT,
    @FieldDoc("A single URL.") URL,
    @FieldDoc("An e-mail address.") EMAIL,
    @FieldDoc("A barcode/ISRC/UPC; clients may offer a scanner.") BARCODE,
    @FieldDoc("Multiple URLs or codes, one per line; clients may offer a scanner that appends lines.") MULTILINE_URLS
}

@Serializable
@ModelDoc("Outcome of a UI action invocation.")
enum class UiInvokeStatus {
    @FieldDoc("The action succeeded.") OK,
    @FieldDoc("Submitted values were rejected; see fieldErrors.") VALIDATION_ERROR,
    @FieldDoc("The action failed.") ERROR,
    @FieldDoc("The user is not allowed to perform the action.") UNAUTHORIZED
}
