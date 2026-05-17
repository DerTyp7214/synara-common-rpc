package dev.dertyp.services.import

import dev.dertyp.PlatformUUID
import dev.dertyp.PrefixedId
import dev.dertyp.data.RequiresAdmin
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.randomPlatformUUID
import dev.dertyp.rpc.annotations.*
import dev.dertyp.serializers.UUIDSerializer
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Rpc
@RpcDoc("Management of the integrated media importer.")
interface IImportService {
    @RestGet
    @RpcDoc("Stream real-time import logs from active processes.")
    fun logs(): Flow<LogLine>
    @RestGet
    @RpcDoc("Get the currently active import task.")
    suspend fun currentImport(): ImportQueueEntry?
    @RestGet
    @RpcDoc("Get the list of pending import tasks in the queue.")
    suspend fun importQueue(): List<ImportQueueEntry>
    @RestGet
    @RpcDoc("Get a list of recently completed or failed import tasks.")
    suspend fun finishedImports(): List<FinishedImportQueueEntry>
    @RestGet
    @RpcDoc("Check if favorite synchronization is available.")
    suspend fun syncFavouritesAvailable(): Boolean
    @RequiresCapability(UserCapability.IMPORT)
    @RpcDoc("Synchronize favorites with the local library.", errors = ["IllegalStateException"])
    suspend fun syncFavourites()
    @RestPost
    @RequiresCapability(UserCapability.IMPORT)
    @RpcDoc("Queue content for import by its IDs.")
    suspend fun importIds(
        @RpcParamDoc("Collection of IDs.") ids: List<PrefixedId>,
        @RpcParamDoc("The type of content (SONG, ALBUM, etc.).") type: Type = Type.SONG,
        @RpcParamDoc("The importer to use.") importer: ImportBackend? = null
    )
    @RestPost
    @RequiresCapability(UserCapability.IMPORT)
    @RpcDoc("Queue content for import by its URLs.")
    suspend fun importUrls(
        @RpcParamDoc("Collection of URLs.") urls: List<String>
    )

    @RestGet
    @RpcDoc("Get the appropriate importer backend for a given URL.")
    suspend fun getImporterForUrl(
        @RpcParamDoc("The URL to check.") url: String
    ): ImportBackend?

    @RestGet
    @RpcDoc("Check if content with a specific original ID is already present in the library.")
    suspend fun existsByOriginalId(
        @RpcParamDoc("The original ID to check.") id: PrefixedId,
        @RpcParamDoc("The type of content.") type: Type = Type.SONG
    ): Boolean
    @RequiresAdmin
    @RpcDoc("Set the preferred importer backend.")
    suspend fun setImportService(@RpcParamDoc("The importer service to use.") service: ImportBackend)
    @RestGet
    @RpcDoc("Get the currently active importer backend.")
    suspend fun getImportService(): ImportBackend

    @RestGet
    @RpcDoc("Get all available importer backends.")
    suspend fun getAllImportServices(): List<ImportBackend>

    @RestGet
    @RpcDoc("Check if the importer is authorized.")
    suspend fun importAuthorized(): Boolean
    @RpcDoc("Trigger the OAuth login flow and stream the login URL.")
    fun importLogin(): Flow<String>

    @RestGet
    @RpcDoc("Check if Tidal favorite synchronization is authorized.")
    suspend fun tidalSyncAuthorized(): Boolean
    @RequiresAdmin
    @RestGet
    @RpcDoc("Get the Tidal OAuth authorization URL.", errors = ["IllegalArgumentException"])
    suspend fun getAuthUrl(): String

    @RequiresAdmin
    @RpcDoc("Immediately stop all active importer processes.")
    suspend fun killAllChildProcesses()

    @RestGet
    @RpcDoc("Search for tracks directly.", errors = ["IllegalStateException"])
    suspend fun search(
        @RpcParamDoc("General search query.") query: String? = null,
        @RpcParamDoc("Filter by track title.") title: String? = null,
        @RpcParamDoc("Filter by artist name.") artist: String? = null,
        @RpcParamDoc("Maximum number of results.") count: Int = 50
    ): List<ImportSong>
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
@ModelDoc("Base class for entries in the import queue.")
sealed class ImportQueueEntry {
    @FieldDoc("The type of content being imported.")
    abstract val type: Type?
    @FieldDoc("Maximum number of retry attempts on failure.")
    abstract val maxRetries: Int
    @FieldDoc("The ID of the user who initiated the import.")
    abstract val byUser: PlatformUUID?
    @FieldDoc("The importer backend to use for this entry.")
    abstract val importer: ImportBackend?
    abstract val callback: suspend () -> Unit
}

@Serializable
@ModelDoc("An import queue entry for content specified by its URL.")
data class UrlImportQueueEntry(
    @FieldDoc("Collection of URLs to import.")
    val urls: MutableList<String>,
    @FieldDoc("Collection of associated IDs.")
    val ids: Collection<String> = emptyList(),
    @Serializable(with = UUIDSerializer::class)
    @FieldDoc("The ID of the user who initiated the import.")
    override val byUser: PlatformUUID? = null,
    @FieldDoc("The type of content.")
    override val type: Type? = null,
    @FieldDoc("The importer backend to use for this entry.")
    override val importer: ImportBackend? = null,
    @Transient
    override val maxRetries: Int = 5,
    @Transient
    override val callback: suspend () -> Unit = {}
) : ImportQueueEntry()

@Serializable
@ModelDoc("An import queue entry for a user's favorite collection.")
data class FavouriteImportQueueEntry(
    @FieldDoc("The type of favorites to import.")
    val favoriteType: ImportFavType,
    @Serializable(with = UUIDSerializer::class)
    @FieldDoc("The ID of the user who initiated the import.")
    override val byUser: PlatformUUID? = null,
    @FieldDoc("The type of content.")
    override val type: Type? = null,
    @FieldDoc("The importer backend to use for this entry.")
    override val importer: ImportBackend? = null,
    @Transient
    override val maxRetries: Int = 5,
    @Transient
    override val callback: suspend () -> Unit = {}
) : ImportQueueEntry()


@Serializable
@ModelDoc("Contains details about a completed or failed import task.")
data class FinishedImportQueueEntry(
    @FieldDoc("The original queue entry.")
    val importQueueEntry: ImportQueueEntry,
    @FieldDoc("The result of the process execution.")
    var result: ProcessExecutionResult,
    @FieldDoc("The full logs from the import process.")
    val logs: List<String>,
)

@Serializable
@ModelDoc("A single log line from an import process.")
data class LogLine(
    @FieldDoc("The queue entry this log belongs to.")
    val queueEntry: ImportQueueEntry,
    @FieldDoc("The actual log text.")
    val line: String?,
)

@Serializable
@ModelDoc("The type of content available for import.")
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
    ARTIST("artist"),

    @SerialName("video")
    VIDEO("video");

    companion object {
        fun fromValue(value: String): Type? {
            return entries.find { it.value == value }
        }
    }
}

@Serializable
@ModelDoc("Metadata for a track found.")
data class ImportSong(
    @FieldDoc("The track ID.")
    val id: String,
    @FieldDoc("The title of the track.")
    val title: String,
    @FieldDoc("Collection of artist names.")
    val artists: List<String>,
    @FieldDoc("Map of cover image sizes to URLs.")
    val cover: Map<Int, String>
)
