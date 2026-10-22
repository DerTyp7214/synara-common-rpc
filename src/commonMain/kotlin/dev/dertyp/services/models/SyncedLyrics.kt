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
    val lines: List<LyricLine>,
    @FieldDoc("The singers of the lyrics by voice id, used by duets and backing vocals.")
    val voices: Map<String, LyricVoice> = emptyMap()
)

@Serializable
@ModelDoc("A singer that lyric lines can be assigned to.")
data class LyricVoice(
    @FieldDoc("The display name of the singer.")
    val name: String,
    @FieldDoc("Whether the voice is a single person or a group.")
    val type: LyricVoiceType = LyricVoiceType.PERSON
)

@Serializable
@ModelDoc("Kind of singer behind a voice.")
enum class LyricVoiceType {
    @FieldDoc("A single person.")
    PERSON,

    @FieldDoc("A group of singers.")
    GROUP
}

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
    val words: List<LyricWord>,
    @FieldDoc("The full text of the line. When set, the word texts joined without a separator equal it exactly. When null, the words are joined with a space.")
    val text: String? = null,
    @FieldDoc("The id of the voice singing the line, as a key of the voices of the lyrics.")
    val voice: String? = null,
    @FieldDoc("Whether the line is a backing vocal.")
    val background: Boolean = false,
    @FieldDoc("Identifier shared by a lead line and the backing vocal lines that were split from the same line.")
    val group: Int = 0
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
