package dev.dertyp.services.models

import dev.dertyp.serializers.DurationMsSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class SyncedLyrics(
    val lines: List<LyricLine>
)

@Serializable
data class LyricLine(
    @Serializable(with = DurationMsSerializer::class)
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    val endTime: Duration,
    val words: List<LyricWord>
)

@Serializable
data class LyricWord(
    val text: String,
    @Serializable(with = DurationMsSerializer::class)
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    val endTime: Duration,
    val chars: List<LyricChar>? = null
)

@Serializable
data class LyricChar(
    val char: String,
    @Serializable(with = DurationMsSerializer::class)
    val startTime: Duration,
    @Serializable(with = DurationMsSerializer::class)
    val endTime: Duration
)
