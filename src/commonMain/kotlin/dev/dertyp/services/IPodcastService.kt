package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.EpisodePlaybackReport
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.PodcastEpisode
import dev.dertyp.data.PodcastEpisodeProgress
import dev.dertyp.data.PodcastScanResult
import dev.dertyp.data.PodcastShow
import dev.dertyp.data.PodcastShowSettings
import dev.dertyp.data.PodcastTranscript
import dev.dertyp.data.PodcastTranscriptContent
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RestDelete
import dev.dertyp.rpc.annotations.RestFileResponse
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RestPut
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Manages podcasts, which are kept completely apart from the music library: an episode is never a song, is never scrobbled and never shows up " +
        "in music search or statistics. A show is either an RSS feed the server follows or a folder of the local podcast library, and a feed show " +
        "is stored once and shared by everyone subscribing to it. Subscriptions and listening positions are private to each user and sync across " +
        "their devices. Episode audio is always played through streamEpisode, no matter whether the server stores the file or relays its origin."
)
interface IPodcastService {
    @RestPost
    @RpcDoc(
        "Subscribe to an RSS feed. The feed is fetched and parsed while the call runs when the server does not know it yet, so the call fails if " +
            "the feed cannot be reached or is not a podcast feed. Subscribing again to a known feed is cheap and does not fetch anything.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun subscribe(
        @RpcParamDoc("The address of the RSS feed to subscribe to. Must be an http or https address of at most 2048 characters.") feedUrl: String
    ): PodcastShow

    @RestPost
    @RpcDoc("Subscribe to a show the server already knows, for example one found through browseShows.", errors = ["IllegalArgumentException"])
    suspend fun subscribeToShow(
        @RpcParamDoc("The show to subscribe to.") showId: PlatformUUID
    ): PodcastShow

    @RestDelete
    @RpcDoc(
        "End the subscription of the user to a show. Returns false if the user was not subscribed. The show itself and its episodes stay on the " +
            "server for a while after the last subscriber leaves."
    )
    suspend fun unsubscribe(
        @RpcParamDoc("The show to unsubscribe from.") showId: PlatformUUID
    ): Boolean

    @RestGet
    @RpcDoc("Read the shows the user is subscribed to, ordered by title.")
    suspend fun getSubscriptions(): List<PodcastShow>

    @RestGet
    @RpcDoc("Browse every show on the server, both feed shows and shows of the local podcast library, optionally narrowed down by a search term.")
    suspend fun browseShows(
        @RpcParamDoc("Search term matched against title, author and description. Blank returns every show.") query: String = "",
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of shows per page, at most 500.") pageSize: Int = 50
    ): PaginatedResponse<PodcastShow>

    @RestGet
    @RpcDoc("Read a single show. Returns null if the show does not exist.")
    suspend fun getShow(
        @RpcParamDoc("The show to read.") showId: PlatformUUID
    ): PodcastShow?

    @RestGet
    @RpcDoc("Read a page of episodes of a show, together with the listening position of the user for each of them.")
    suspend fun getEpisodes(
        @RpcParamDoc("The show to read the episodes of.") showId: PlatformUUID,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of episodes per page, at most 500.") pageSize: Int = 50,
        @RpcParamDoc("Whether the newest episode comes first.") newestFirst: Boolean = true
    ): PaginatedResponse<PodcastEpisode>

    @RestGet
    @RpcDoc("Read a single episode together with the listening position of the user. Returns null if the episode does not exist.")
    suspend fun getEpisode(
        @RpcParamDoc("The episode to read.") episodeId: PlatformUUID
    ): PodcastEpisode?

    @RestGet
    @RpcDoc("Search episodes of every show on the server by their title, their description and the title of their show.", errors = ["IllegalArgumentException"])
    suspend fun searchEpisodes(
        @RpcParamDoc("The search term. Must not be blank.") query: String,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of episodes per page, at most 500.") pageSize: Int = 50
    ): PaginatedResponse<PodcastEpisode>

    @RestGet
    @RpcDoc("Read the newest episodes across the shows the user is subscribed to, newest first.")
    suspend fun getLatestEpisodes(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of episodes per page, at most 500.") pageSize: Int = 50
    ): PaginatedResponse<PodcastEpisode>

    @RestGet
    @RpcDoc("Read the episodes the user started but did not finish, the most recently played one first.")
    suspend fun getInProgress(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of episodes per page, at most 500.") pageSize: Int = 50
    ): PaginatedResponse<PodcastEpisode>

    @RestPost
    @RpcDoc(
        "Store the listening position of the user for an episode. A client reports on play, on pause, after a seek and every 10 to 15 seconds " +
            "while playing, the same way song playback is reported. The last report wins, and the server marks an episode as completed on its own " +
            "once the remaining time is under 30 seconds or under 5 percent of its length.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun reportPlayback(
        @RpcParamDoc("What the client is playing and how far it has got.") report: EpisodePlaybackReport
    ): PodcastEpisodeProgress

    @RestPut
    @RpcDoc(
        "Mark an episode as listened or as unlistened. Marking it as listened moves the position to the end of the episode, marking it as " +
            "unlistened resets the position to the beginning.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun setPlayed(
        @RpcParamDoc("The episode to mark.") episodeId: PlatformUUID,
        @RpcParamDoc("Whether the episode counts as listened.") played: Boolean
    ): PodcastEpisodeProgress

    @RestGet
    @RpcDoc("Watch the listening positions of the user for changes, so other devices follow along while an episode is played somewhere else.")
    fun observeProgress(): Flow<PodcastEpisodeProgress>

    @RestPost
    @RpcDoc(
        "Fetch the feed of a show right now and store what changed. The call runs to completion before it returns and is skipped when the feed " +
            "was already fetched less than 60 seconds ago.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun refreshShow(
        @RpcParamDoc("The show to refresh.") showId: PlatformUUID
    ): PodcastShow

    @RestPut
    @RequiresCapability(UserCapability.PODCAST_EDIT)
    @RpcDoc(
        "Change how the server keeps the episodes of a show. Switching a show to import makes the server store new episodes on disk, and the " +
            "number of episodes to keep must be at least 1 when it is given. With newest retention the server imports the newest episodes and " +
            "deletes everything beyond that count, while unlistened retention, which needs import delivery, imports the episodes nobody has " +
            "finished yet and only deletes an episode once every subscriber listened to it to the end, with the keep count capping how many " +
            "episodes are stored or queued at once.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun updateShowSettings(
        @RpcParamDoc("The show to change.") showId: PlatformUUID,
        @RpcParamDoc("The settings to store for the show.") settings: PodcastShowSettings
    ): PodcastShow

    @RestPost
    @RequiresCapability(UserCapability.PODCAST_EDIT)
    @RpcDoc(
        "Queue a single episode to be stored on the server. The transfer itself runs in the background, so the returned episode usually reports " +
            "that its import is queued.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun importEpisode(
        @RpcParamDoc("The episode to store on the server.") episodeId: PlatformUUID
    ): PodcastEpisode

    @RestDelete
    @RequiresCapability(UserCapability.PODCAST_EDIT)
    @RpcDoc(
        "Delete the stored audio of an episode of a feed show. The episode itself stays and is played from its origin afterwards. Episodes of the " +
            "local podcast library cannot be removed this way, as that would delete the file of the user.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun removeImport(
        @RpcParamDoc("The episode whose stored audio is deleted.") episodeId: PlatformUUID
    ): PodcastEpisode

    @RestPost
    @RequiresCapability(UserCapability.PODCAST_EDIT)
    @RpcDoc("Scan the local podcast library for new, changed and vanished shows and episodes. The call runs to completion before it returns.")
    suspend fun scanLocal(): PodcastScanResult

    @RestFileResponse
    @RpcDoc(
        "Stream the audio of an episode. This is the only way a client plays an episode: the server serves the file when the episode is stored " +
            "on it or belongs to the local podcast library, and relays the audio from its origin otherwise. Returns null if the episode has no " +
            "audio at all."
    )
    fun streamEpisode(
        @RpcParamDoc("The episode to play.") episodeId: PlatformUUID,
        @RpcParamDoc("Byte offset to start streaming from.") offset: Long = 0,
        @RpcParamDoc("Size of each data chunk.") chunkSize: Int = 4096
    ): Flow<ByteArray>?

    @RestGet
    @RpcDoc("Get the total size of the audio of an episode in bytes, or 0 if the size cannot be determined.")
    suspend fun getStreamSize(
        @RpcParamDoc("The episode to read the size of.") episodeId: PlatformUUID
    ): Long

    @RestGet
    @RpcDoc("Read the transcripts known for an episode without their text.")
    suspend fun getTranscripts(
        @RpcParamDoc("The episode to read the transcripts of.") episodeId: PlatformUUID
    ): List<PodcastTranscript>

    @RestGet
    @RpcDoc(
        "Read a single transcript together with its text. A transcript that only exists as a remote address is fetched once on first read and " +
            "kept afterwards. Returns null if the transcript does not exist or its text cannot be obtained."
    )
    suspend fun getTranscript(
        @RpcParamDoc("The transcript to read.") transcriptId: PlatformUUID
    ): PodcastTranscriptContent?
}
