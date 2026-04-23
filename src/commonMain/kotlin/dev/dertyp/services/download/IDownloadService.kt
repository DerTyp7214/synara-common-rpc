package dev.dertyp.services.download

import dev.dertyp.PlatformUUID
import dev.dertyp.randomPlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.serializers.UUIDSerializer
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Rpc
@RpcDoc("Management of the integrated media downloader.")
interface IDownloadService {
    @RestGet
    @RpcDoc("Stream real-time download logs from active processes.")
    fun logs(): Flow<LogLine>
    @RestGet
    @RpcDoc("Get the currently active download task.")
    suspend fun currentDownload(): DownloadQueueEntry?
    @RestGet
    @RpcDoc("Get the list of pending download tasks in the queue.")
    suspend fun downloadQueue(): List<DownloadQueueEntry>
    @RestGet
    @RpcDoc("Get a list of recently completed or failed download tasks.")
    suspend fun finishedDownloads(): List<FinishedDownloadQueueEntry>
    @RestGet
    @RpcDoc("Check if favorite synchronization is available.")
    suspend fun syncFavouritesAvailable(): Boolean
    @RpcDoc("Synchronize favorites with the local library.", errors = ["IllegalStateException"])
    suspend fun syncFavourites()
    @RestPost
    @RpcDoc("Queue content for download by its IDs.")
    suspend fun downloadIds(
        @RpcParamDoc("Collection of IDs.") ids: List<String>,
        @RpcParamDoc("The type of content (SONG, ALBUM, etc.).") type: Type = Type.SONG
    )
    @RestPost
    @RpcDoc("Queue content for download by its URLs.")
    suspend fun downloadUrls(
        @RpcParamDoc("Collection of URLs.") urls: List<String>
    )
    @RestGet
    @RpcDoc("Check if content with a specific original ID is already present in the library.")
    suspend fun existsByOriginalId(
        @RpcParamDoc("The original ID to check.") id: String,
        @RpcParamDoc("The type of content.") type: Type = Type.SONG
    ): Boolean
    @RpcDoc("Set the preferred downloader backend.")
    suspend fun setDownloadService(@RpcParamDoc("The downloader service to use.") service: DownloadBackend)
    @RestGet
    @RpcDoc("Get the currently active downloader backend.")
    suspend fun getDownloadService(): DownloadBackend

    @RestGet
    @RpcDoc("Check if the downloader is authorized.")
    suspend fun downloadAuthorized(): Boolean
    @RpcDoc("Trigger the OAuth login flow and stream the login URL.")
    fun downloadLogin(): Flow<String>

    @RestGet
    @RpcDoc("Check if Tidal favorite synchronization is authorized.")
    suspend fun tidalSyncAuthorized(): Boolean
    @RestGet
    @RpcDoc("Get the Tidal OAuth authorization URL.", errors = ["IllegalArgumentException"])
    suspend fun getAuthUrl(): String

    @RpcDoc("Immediately stop all active downloader processes.")
    suspend fun killAllChildProcesses()

    @RestGet
    @RpcDoc("Search for tracks directly.", errors = ["IllegalStateException"])
    suspend fun search(
        @RpcParamDoc("General search query.") query: String? = null,
        @RpcParamDoc("Filter by track title.") title: String? = null,
        @RpcParamDoc("Filter by artist name.") artist: String? = null,
        @RpcParamDoc("Maximum number of results.") count: Int = 50
    ): List<DownloadSong>
}

data class IdsWrapper(
    val type: Type,
    val idGroups: Flow<IdsGroup>
) {
    suspend fun size() = getIds().toList().size

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getIds(): Flow<String> = idGroups.flatMapConcat { it.ids.map { entry -> entry.second } }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun filter(predicate: (entry: Pair<Long, String>) -> Boolean) =
        idGroups.map { it.filter(predicate) }.flattenConcat()

    fun applyFilter(predicate: (entry: Pair<Long, String>) -> Boolean) =
        copy(
            idGroups = idGroups.map { it.copy(ids = it.ids.filter(predicate)) }.filter { it.ids.toList().isNotEmpty() }
        )

    fun fetchExistingSongs() = when (type) {
        Type.SONG -> true
        else -> false
    }

    fun logSize() = when (type) {
        Type.SONG, Type.ARTIST -> true
        else -> false
    }

    companion object {
        fun from(type: Type, ids: Map<Long, String>): IdsWrapper {
            return IdsWrapper(
                type = type,
                idGroups = listOf(
                    IdsGroup(
                        ids = ids.toList().asFlow()
                    )
                ).asFlow()
            )
        }
    }
}

data class IdsGroup(
    val id: String = randomPlatformUUID().toString(),
    val ids: Flow<Pair<Long, String>>,
    val metadata: IMetadataService.BaseMetadata? = null
) {
    fun filter(predicate: (entry: Pair<Long, String>) -> Boolean) = ids.filter(predicate)
}

@Serializable
@ModelDoc("Base class for entries in the download queue.")
sealed class DownloadQueueEntry {
    @FieldDoc("The type of content being downloaded.")
    abstract val type: Type?
    @FieldDoc("Maximum number of retry attempts on failure.")
    abstract val maxRetries: Int
    @FieldDoc("The ID of the user who initiated the download.")
    abstract val byUser: PlatformUUID?
    abstract val callback: suspend () -> Unit

    open fun type(): Type? {
        return type
    }
}

@Serializable
@ModelDoc("A download queue entry for content specified by its URL.")
data class UrlDownloadQueueEntry(
    @FieldDoc("Collection of URLs to download.")
    val urls: MutableList<String>,
    @FieldDoc("Collection of associated IDs.")
    val ids: Collection<String> = emptyList(),
    @Serializable(with = UUIDSerializer::class)
    @FieldDoc("The ID of the user who initiated the download.")
    override val byUser: PlatformUUID? = null,
    @FieldDoc("The type of content.")
    override val type: Type? = null,
    @Transient
    override val maxRetries: Int = 5,
    @Transient
    override val callback: suspend () -> Unit = {}
) : DownloadQueueEntry() {
    override fun type(): Type? {
        if (type != null) return type

        val url = urls.first()

        return when {
            url.contains("/mix/") -> Type.MIX
            url.contains("/track/") -> Type.SONG
            url.contains("/album/") -> Type.ALBUM
            url.contains("/artist/") -> Type.ARTIST
            url.contains("/playlist/") -> Type.PLAYLIST
            else -> null
        }
    }
}

@Serializable
@ModelDoc("A download queue entry for a user's favorite collection.")
data class FavouriteDownloadQueueEntry(
    @FieldDoc("The type of favorites to download.")
    val favoriteType: DownloadFavType,
    @Serializable(with = UUIDSerializer::class)
    @FieldDoc("The ID of the user who initiated the download.")
    override val byUser: PlatformUUID? = null,
    @FieldDoc("The type of content.")
    override val type: Type? = null,
    @Transient
    override val maxRetries: Int = 5,
    @Transient
    override val callback: suspend () -> Unit = {}
) : DownloadQueueEntry()


@Serializable
@ModelDoc("Contains details about a completed or failed download task.")
data class FinishedDownloadQueueEntry(
    @FieldDoc("The original queue entry.")
    val downloadQueueEntry: DownloadQueueEntry,
    @FieldDoc("The result of the process execution.")
    var result: ProcessExecutionResult,
    @FieldDoc("The full logs from the download process.")
    val logs: List<String>,
)

@Serializable
@ModelDoc("A single log line from a download process.")
data class LogLine(
    @FieldDoc("The queue entry this log belongs to.")
    val queueEntry: DownloadQueueEntry,
    @FieldDoc("The actual log text.")
    val line: String?,
)

@Serializable
@ModelDoc("The type of content available for download.")
enum class Type(val value: String) {
    @SerialName("mix")
    MIX("mix"),

    @SerialName("track")
    SONG("track"),

    @SerialName("album")
    ALBUM("album"),

    @SerialName("playlist")
    PLAYLIST("playlist"),

    @SerialName("artist")
    ARTIST("artist");

    companion object {
        fun fromValue(value: String): Type? {
            return entries.find { it.value == value }
        }
    }
}

@Serializable
@ModelDoc("Metadata for a track found.")
data class DownloadSong(
    @FieldDoc("The track ID.")
    val id: String,
    @FieldDoc("The title of the track.")
    val title: String,
    @FieldDoc("Collection of artist names.")
    val artists: List<String>,
    @FieldDoc("Map of cover image sizes to URLs.")
    val cover: Map<Int, String>
)
