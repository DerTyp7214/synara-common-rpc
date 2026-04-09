package dev.dertyp.services.models

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DurationMsSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
@ModelDoc("A collection of time-synced lyrics for a song.")
data class SyncedLyrics(
    @FieldDoc("List of individual lyric lines.")
    val lines: List<LyricLine>
)

@Serializable
@ModelDoc("A single line of lyrics with timing information.")
data class LyricLine(
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the line starts.")
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the line ends.")
    val endTime: Duration,
    @FieldDoc("Collection of individual words in the line.")
    val words: List<LyricWord>
)

@Serializable
@ModelDoc("A single word in a synced lyric line with precise timing.")
data class LyricWord(
    @FieldDoc("The text of the word.")
    val text: String,
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the word starts.")
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the word ends.")
    val endTime: Duration,
    @FieldDoc("Optional character-level timing information.")
    val chars: List<LyricChar>? = null
)

@Serializable
@ModelDoc("A single character in a synced lyric word with precise timing.")
data class LyricChar(
    @FieldDoc("The character text.")
    val char: String,
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the character starts.")
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    @FieldDoc("Timestamp of when the character ends.")
    val endTime: Duration
)
