@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A time range for listening statistics.")
enum class StatsRange { DAY, WEEK, MONTH, YEAR, ALL_TIME }

@Serializable
@ModelDoc("Comparison of the current range's listen count against the previous equivalent range.")
data class RangeComparison(
    @FieldDoc("Start of the previous range (epoch milliseconds, inclusive).")
    val previousStart: Long,
    @FieldDoc("End of the previous range (epoch milliseconds, exclusive).")
    val previousEnd: Long,
    @FieldDoc("Deduplicated listen count in the previous range.")
    val previousCount: Long,
    @FieldDoc("Percent change of the current count relative to the previous count, or null if the previous range had no listens.")
    val percentChange: Double?,
)

@Serializable
@ModelDoc("A song ranked by listen count. Fallback entries for listens not matched to a library song carry a null songId.")
data class TopSongEntry(
    @FieldDoc("The library song, or null for listens not matched to the library.")
    val songId: PlatformUUID?,
    @FieldDoc("The song title.")
    val title: String,
    @FieldDoc("The artist name, or null if unknown.")
    val artistName: String?,
    @FieldDoc("The album name, or null if unknown.")
    val albumName: String?,
    @FieldDoc("The song's cover image, or null if none.")
    val coverId: PlatformUUID?,
    @FieldDoc("Deduplicated listen count in the range.")
    val listenCount: Long,
    @FieldDoc("The MusicBrainz recording MBID of an unmatched entry, or null.")
    val recordingMbid: PlatformUUID? = null,
    @FieldDoc("A representative ListenBrainz recording MSID of an unmatched entry, or null. When recordingMbid and recordingMsid are both null the entry cannot be linked.")
    val recordingMsid: PlatformUUID? = null,
)

@Serializable
@ModelDoc("An artist ranked by listen count. Fallback entries for listens not matched to a library artist carry a null artistId.")
data class TopArtistEntry(
    @FieldDoc("The library artist, or null for listens not matched to the library.")
    val artistId: PlatformUUID?,
    @FieldDoc("The artist name.")
    val name: String,
    @FieldDoc("The artist's image, or null if none.")
    val imageId: PlatformUUID?,
    @FieldDoc("Deduplicated listen count in the range.")
    val listenCount: Long,
)

@Serializable
@ModelDoc("An album ranked by listen count. Fallback entries for listens not matched to a library album carry a null albumId.")
data class TopAlbumEntry(
    @FieldDoc("The library album, or null for listens not matched to the library.")
    val albumId: PlatformUUID?,
    @FieldDoc("The album name.")
    val name: String,
    @FieldDoc("The album's cover image, or null if none.")
    val coverId: PlatformUUID?,
    @FieldDoc("Deduplicated listen count in the range.")
    val listenCount: Long,
)

@Serializable
@ModelDoc("Distribution of listens over the hours of the day and days of the week, in the requested timezone.")
data class ListenClock(
    @FieldDoc("Listen counts per hour of day; 24 entries, index 0 = 00:00-00:59.")
    val hourOfDay: List<Long>,
    @FieldDoc("Listen counts per day of week; 7 entries, index 0 = Monday.")
    val dayOfWeek: List<Long>,
)

@Serializable
@ModelDoc("Daily listening streaks, computed over the full listen history in the requested timezone.")
data class ListeningStreaks(
    @FieldDoc("Consecutive days with at least one listen, ending today or yesterday.")
    val currentStreakDays: Int,
    @FieldDoc("Longest run of consecutive days with at least one listen.")
    val longestStreakDays: Int,
)

@Serializable
@ModelDoc("Songs and artists listened to for the first time within the range.")
data class Discoveries(
    @FieldDoc("Songs first listened to within the range, ranked by listen count.")
    val songs: List<TopSongEntry>,
    @FieldDoc("Artists first listened to within the range, ranked by listen count.")
    val artists: List<TopArtistEntry>,
)

@Serializable
@ModelDoc("Request to link a user's unmatched listens of a track to a library song, identified by recording MSID and/or MBID.")
data class LinkUnmatchedTrackRequest(
    @FieldDoc("The library song to link the listens to.")
    val songId: PlatformUUID,
    @FieldDoc("A ListenBrainz recording MSID of the unmatched track; the server expands it to the whole group.")
    val recordingMsid: PlatformUUID? = null,
    @FieldDoc("The MusicBrainz recording MBID of the unmatched track.")
    val recordingMbid: PlatformUUID? = null,
)

@Serializable
@ModelDoc("Result of linking unmatched listens to a library song.")
data class LinkUnmatchedTrackResult(
    @FieldDoc("Number of listens that were linked to the song.")
    val linkedListens: Int,
    @FieldDoc("Number of manual mappings accepted by the ListenBrainz API; 0 when the song has no recording MBID, no account token is stored, or no listens carry an MSID.")
    val submittedToListenBrainz: Int,
)

@Serializable
@ModelDoc("Listening statistics for a time range over the user's unified listen history, deduplicated across local scrobbles and ListenBrainz imports.")
data class ListeningStats(
    @FieldDoc("The requested time range.")
    val range: StatsRange,
    @FieldDoc("The timezone used for range boundaries.")
    val timezone: String,
    @FieldDoc("Start of the range (epoch milliseconds, inclusive); 0 for ALL_TIME.")
    val rangeStart: Long,
    @FieldDoc("End of the range (epoch milliseconds, exclusive).")
    val rangeEnd: Long,
    @FieldDoc("Deduplicated listen count in the range.")
    val listenCount: Long,
    @FieldDoc("Comparison against the previous equivalent range, or null for ALL_TIME.")
    val comparison: RangeComparison?,
    @FieldDoc("Distinct songs listened to in the range.")
    val uniqueSongs: Int,
    @FieldDoc("Distinct artists listened to in the range.")
    val uniqueArtists: Int,
    @FieldDoc("Distinct albums listened to in the range.")
    val uniqueAlbums: Int,
    @FieldDoc("Most listened songs in the range.")
    val topSongs: List<TopSongEntry>,
    @FieldDoc("Most listened artists in the range.")
    val topArtists: List<TopArtistEntry>,
    @FieldDoc("Most listened albums in the range.")
    val topAlbums: List<TopAlbumEntry>,
    @FieldDoc("Hour-of-day and day-of-week listen distribution for the range.")
    val listenClock: ListenClock,
    @FieldDoc("Daily listening streaks over the full history.")
    val streaks: ListeningStreaks,
    @FieldDoc("Songs and artists first listened to within the range.")
    val discoveries: Discoveries,
)
