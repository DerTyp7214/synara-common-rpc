@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services.models

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("How finely lyrics are synced to the audio.")
enum class LyricsSyncLevel {
    @FieldDoc("Text only, without timing.")
    PLAIN,

    @FieldDoc("Timed per line.")
    LINE,

    @FieldDoc("Timed per word or syllable.")
    WORD
}

@Serializable
@ModelDoc("How sure it is that lyrics belong to the song.")
enum class LyricsMatch {
    @FieldDoc("The title, artist and duration of the lyrics were checked against the song.")
    VERIFIED,

    @FieldDoc("The provider returned the lyrics for the song, but did not tell which track they are for.")
    PROVIDER
}

@Serializable
@ModelDoc("File format of lyrics that are imported.")
enum class LyricsImportFormat {
    @FieldDoc("LRC with line stamps and optional word stamps.")
    LRC,

    @FieldDoc("TTML with optionally timed words.")
    TTML,

    @FieldDoc("A lyricsfile in YAML.")
    LYRICSFILE
}

@Serializable
@ModelDoc("A lyrics file to import.")
data class LyricsImport(
    @FieldDoc("The format of the content.")
    val format: LyricsImportFormat,
    @FieldDoc("The content of the file.")
    val content: String
)

@Serializable
@ModelDoc("A source of lyrics.")
data class LyricsProviderInfo(
    @FieldDoc("The provider identifier, such as betterlyrics or kugou.")
    val id: String,
    @FieldDoc("The display name of the provider.")
    val name: String,
    @FieldDoc("The web address of the provider to credit, or null if it has none.")
    val url: String? = null,
    @FieldDoc("Whether lookups ask this provider.")
    val enabled: Boolean,
    @FieldDoc("Position of the provider in the configured order, lower is asked and preferred first.")
    val priority: Int
)

@Serializable
@ModelDoc("One version of the lyrics of a song as offered by a provider.")
data class LyricsCandidate(
    @FieldDoc("The candidate unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The song unique identifier, or null if the candidate is not tied to a song.")
    val songId: PlatformUUID?,
    @FieldDoc("The provider the candidate comes from.")
    val provider: LyricsProviderInfo,
    @FieldDoc("How finely the lyrics are synced.")
    val syncLevel: LyricsSyncLevel,
    @FieldDoc("Whether the track is instrumental.")
    val instrumental: Boolean,
    @FieldDoc("How sure it is that the lyrics belong to the song.")
    val match: LyricsMatch,
    @FieldDoc("Difference in seconds between the duration of the lyrics and of the song, or null if unknown.")
    val durationGapSec: Int?,
    @FieldDoc("Position among the candidates of the song, 1 is best.")
    val rank: Int,
    @FieldDoc("Whether an admin pinned this candidate as the default.")
    val preferred: Boolean,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of when the candidate was fetched.")
    val fetchedAt: PlatformDate,
    @FieldDoc("The plain text of the lyrics.")
    val plain: String,
    @FieldDoc("The synced lyrics. Only filled when the content was requested.")
    val lyrics: SyncedLyrics? = null
)

@Serializable
@ModelDoc("Lyrics candidates found for a song or a query.")
data class LyricsSearchResult(
    @FieldDoc("The candidates ordered by rank.")
    val candidates: List<LyricsCandidate>,
    @FieldDoc("The identifier of the default candidate. This is the pinned one, otherwise the best ranked one.")
    val defaultCandidateId: PlatformUUID?,
    @FieldDoc("Identifiers of the providers that did not answer in time and can be asked again later.")
    val pending: List<String> = emptyList()
)
