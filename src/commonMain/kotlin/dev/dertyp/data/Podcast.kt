@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Where the episodes of a podcast show come from.")
enum class PodcastSource {
    @FieldDoc("The show is an RSS feed the server fetches and keeps up to date.") FEED,

    @FieldDoc("The show is a folder of audio files in the local podcast library of the server.") LOCAL
}

@Serializable
@ModelDoc("Decides whether the server only relays the audio of a feed show or stores its episodes on disk.")
enum class PodcastDeliveryMode {
    @FieldDoc("Episodes stay at their origin and the server relays their audio while a client plays them.") STREAM,

    @FieldDoc("New episodes are imported to the storage of the server so clients play them from the server itself.") IMPORT
}

@Serializable
@ModelDoc("The role an episode plays within its show.")
enum class PodcastEpisodeType {
    @FieldDoc("A regular episode of the show.") FULL,

    @FieldDoc("A short preview of the show or of a season.") TRAILER,

    @FieldDoc("Extra material that is not part of the regular run of the show.") BONUS
}

@Serializable
@ModelDoc("How far the server has got with storing the audio of an episode on disk.")
enum class PodcastImportState {
    @FieldDoc("The audio of the episode is not stored on the server.") NONE,

    @FieldDoc("The episode is waiting to be imported.") QUEUED,

    @FieldDoc("The audio of the episode is being transferred to the server right now.") IMPORTING,

    @FieldDoc("The audio of the episode is stored on the server.") IMPORTED,

    @FieldDoc("The last import attempt failed; the server retries a few times before giving up.") FAILED
}

@Serializable
@ModelDoc(
    "A podcast show, either an RSS feed the server follows or a folder of the local podcast library. A feed show is stored once and shared by " +
        "everyone who subscribes to it, while subscriptions themselves are private to each user."
)
data class PodcastShow(
    @FieldDoc("The unique identifier of the show.")
    val id: PlatformUUID,
    @FieldDoc("Where the episodes of the show come from.")
    val source: PodcastSource,
    @FieldDoc("The address of the RSS feed of the show, or null for a show of the local podcast library.")
    val feedUrl: String? = null,
    @FieldDoc("The folder of the show inside the local podcast library, or null for a feed show.")
    val localPath: String? = null,
    @FieldDoc("The title of the show.")
    val title: String,
    @FieldDoc("The description of the show. May be blank.")
    val description: String = "",
    @FieldDoc("The author or publisher of the show, if the source names one.")
    val author: String? = null,
    @FieldDoc("The language of the show as reported by its source, for example en or de-DE.")
    val language: String? = null,
    @FieldDoc("The website of the show, if the source names one.")
    val link: String? = null,
    @FieldDoc("The artwork of the show, or null if the show has none.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("Whether the show is marked as explicit by its source.")
    val explicit: Boolean = false,
    @FieldDoc("Whether the server only relays the audio of the show or stores its episodes on disk.")
    val deliveryMode: PodcastDeliveryMode = PodcastDeliveryMode.STREAM,
    @FieldDoc("How many of the newest episodes are kept on disk while the show is imported, or null to keep every imported episode.")
    val keepEpisodes: Int? = null,
    @FieldDoc("Unix timestamp in milliseconds of the last time the feed of the show was fetched, or null if it was never fetched.")
    val lastFetchedAt: Long? = null,
    @FieldDoc("The error of the last failed fetch of the feed, or null if the last fetch succeeded.")
    val lastFetchError: String? = null,
    @FieldDoc("Number of episodes the show currently has.")
    val episodeCount: Int = 0,
    @FieldDoc("Number of users subscribed to the show.")
    val subscriberCount: Int = 0,
    @FieldDoc("Whether the calling user is subscribed to the show.")
    val subscribed: Boolean = false,
    @FieldDoc("Unix timestamp in milliseconds at which the show was added to the server.")
    val createdAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last change to the show.")
    val updatedAt: Long
)

@Serializable
@ModelDoc(
    "A single episode of a podcast show. An episode is never a song: it is not scrobbled, does not appear in the music library and carries its " +
        "own listening position per user."
)
data class PodcastEpisode(
    @FieldDoc("The unique identifier of the episode.")
    val id: PlatformUUID,
    @FieldDoc("The show the episode belongs to.")
    val showId: PlatformUUID,
    @FieldDoc("The title of the show the episode belongs to.")
    val showTitle: String,
    @FieldDoc("The identifier the source of the show gives the episode. Unique within the show.")
    val guid: String,
    @FieldDoc("The title of the episode.")
    val title: String,
    @FieldDoc("The description or show notes of the episode. May be blank and may contain HTML.")
    val description: String = "",
    @FieldDoc("The website of the episode, if the source names one.")
    val link: String? = null,
    @FieldDoc("Unix timestamp in milliseconds at which the episode was published.")
    val publishedAt: Long,
    @FieldDoc("The length of the episode in milliseconds, or null if it is unknown.")
    val durationMs: Long? = null,
    @FieldDoc(
        "The address of the audio at its origin, purely informational. Clients never fetch it themselves and always play an episode through " +
            "streamEpisode, which serves the stored file or relays the origin as needed."
    )
    val enclosureUrl: String? = null,
    @FieldDoc("The media type of the audio as announced by the source, for example audio/mpeg.")
    val enclosureType: String? = null,
    @FieldDoc("The size of the audio in bytes as announced by the source, or null if it is unknown.")
    val enclosureLength: Long? = null,
    @FieldDoc("The artwork of the episode, or null if the episode has none of its own.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("The artwork of the show the episode belongs to, so a client can fall back to it.")
    val showImageId: PlatformUUID? = null,
    @FieldDoc("The season the episode belongs to, if the source names one.")
    val seasonNumber: Int? = null,
    @FieldDoc("The number of the episode within its season or show, if the source names one.")
    val episodeNumber: Int? = null,
    @FieldDoc("The role the episode plays within its show.")
    val episodeType: PodcastEpisodeType = PodcastEpisodeType.FULL,
    @FieldDoc("Whether the episode is marked as explicit by its source.")
    val explicit: Boolean = false,
    @FieldDoc("How far the server has got with storing the audio of the episode on disk.")
    val importState: PodcastImportState = PodcastImportState.NONE,
    @FieldDoc("Whether the audio of the episode is stored on the server and can be played without reaching its origin.")
    val imported: Boolean = false,
    @FieldDoc("Unix timestamp in milliseconds at which the audio of the episode was stored on the server, or null if it is not stored.")
    val importedAt: Long? = null,
    @FieldDoc("The size of the stored audio in bytes, or null if the audio is not stored on the server.")
    val fileSize: Long? = null,
    @FieldDoc("The format of the stored audio, for example mp3, or null if the audio is not stored on the server.")
    val format: String? = null,
    @FieldDoc("Whether at least one transcript is known for the episode.")
    val hasTranscript: Boolean = false,
    @FieldDoc("The listening position of the calling user, or null if the user never played the episode.")
    val progress: PodcastEpisodeProgress? = null,
    @FieldDoc("Unix timestamp in milliseconds at which the episode was added to the server.")
    val createdAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last change to the episode.")
    val updatedAt: Long
)

@Serializable
@ModelDoc("A transcript of an episode, announced by the feed, found next to a local file or embedded in the audio itself.")
data class PodcastTranscript(
    @FieldDoc("The unique identifier of the transcript.")
    val id: PlatformUUID,
    @FieldDoc("The episode the transcript belongs to.")
    val episodeId: PlatformUUID,
    @FieldDoc("The media type of the transcript, for example text/vtt, application/srt, application/json or text/plain.")
    val type: String,
    @FieldDoc("The language of the transcript, if it is known.")
    val language: String? = null,
    @FieldDoc("The relation the transcript has to the episode, for example captions.")
    val rel: String? = null,
    @FieldDoc("Whether the content of the transcript can be read; a transcript that only exists as a remote address becomes available on first read.")
    val available: Boolean
)

@Serializable
@ModelDoc("A transcript together with its text.")
data class PodcastTranscriptContent(
    @FieldDoc("The transcript the text belongs to.")
    val transcript: PodcastTranscript,
    @FieldDoc("The text of the transcript in the format the transcript announces.")
    val content: String
)

@Serializable
@ModelDoc("How far a user has listened to an episode. Progress is private to the user and shared between all of their devices.")
data class PodcastEpisodeProgress(
    @FieldDoc("The episode the position belongs to.")
    val episodeId: PlatformUUID,
    @FieldDoc("The show the episode belongs to.")
    val showId: PlatformUUID,
    @FieldDoc("The listening position in the episode in milliseconds.")
    val positionMs: Long,
    @FieldDoc("The length of the episode in milliseconds as the client reported it, or null if it is unknown.")
    val durationMs: Long? = null,
    @FieldDoc("Whether the episode counts as listened to the end.")
    val completed: Boolean,
    @FieldDoc("Unix timestamp in milliseconds of the last time the user played the episode.")
    val lastPlayedAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last change to the position.")
    val updatedAt: Long,
    @FieldDoc("The device that reported the position last, or null if the client did not name one.")
    val deviceId: String? = null
)

@Serializable
@ModelDoc("What a client reports about the episode it is playing so the server can keep the listening position of the user up to date.")
data class EpisodePlaybackReport(
    @FieldDoc("The episode being played.")
    val episodeId: PlatformUUID,
    @FieldDoc("The current listening position in the episode in milliseconds. Must not be negative.")
    val positionMs: Long,
    @FieldDoc("The length of the episode in milliseconds as the client knows it, or null to let the server use the length it stored.")
    val durationMs: Long? = null,
    @FieldDoc("Whether the client considers the episode finished. The server also completes an episode on its own near its end.")
    val completed: Boolean = false,
    @FieldDoc("A stable identifier of the device the episode is played on, at most 64 characters.")
    val deviceId: String? = null
)

@Serializable
@ModelDoc("The settings of a show that decide how the server keeps its episodes.")
data class PodcastShowSettings(
    @FieldDoc("Whether the server only relays the audio of the show or stores its episodes on disk.")
    val deliveryMode: PodcastDeliveryMode,
    @FieldDoc("How many of the newest episodes to keep on disk while the show is imported, at least 1, or null to keep every imported episode.")
    val keepEpisodes: Int? = null
)

@Serializable
@ModelDoc("What a scan of the local podcast library changed.")
data class PodcastScanResult(
    @FieldDoc("Number of shows found in the local podcast library.")
    val shows: Int,
    @FieldDoc("Number of episodes added by the scan.")
    val episodesAdded: Int,
    @FieldDoc("Number of episodes updated by the scan.")
    val episodesUpdated: Int,
    @FieldDoc("Number of episodes removed because their file is gone.")
    val episodesRemoved: Int,
    @FieldDoc("Number of shows removed because their folder is gone.")
    val showsRemoved: Int
)
