package dev.dertyp.services.tdn

import dev.dertyp.PlatformUUID
import dev.dertyp.randomPlatformUUID
import dev.dertyp.serializers.UUIDSerializer
import dev.dertyp.services.metadata.IMetadataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Rpc
interface IDownloadService {
    fun logs(): Flow<LogLine>
    suspend fun currentDownload(): DownloadQueueEntry?
    suspend fun downloadQueue(): List<DownloadQueueEntry>
    suspend fun finishedDownloads(): List<FinishedDownloadQueueEntry>
    suspend fun syncFavouritesAvailable(): Boolean
    suspend fun syncFavourites()
    suspend fun downloadTidalIds(ids: List<String>, type: Type = Type.SONG)
    suspend fun existsByTidalId(id: String, type: Type = Type.SONG): Boolean
    suspend fun setTidalDownloadService(service: TidalDownloadService)
    suspend fun getTidalDownloadService(): TidalDownloadService

    suspend fun tidalDownloadAuthorized(): Boolean
    fun tidalDownloadLogin(): Flow<String>

    suspend fun tidalSyncAuthorized(): Boolean
    suspend fun getAuthUrl(): String

    suspend fun killAllChildProcesses()

    suspend fun searchTidal(query: String? = null, title: String? = null, artist: String? = null, count: Int = 50): List<TidalSong>
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
sealed class DownloadQueueEntry {
    abstract val type: Type?
    abstract val maxRetries: Int
    abstract val byUser: PlatformUUID?
    abstract val callback: suspend () -> Unit

    open fun type(): Type? {
        return type
    }
}

@Serializable
data class UrlDownloadQueueEntry(
    val urls: MutableList<String>,
    val ids: Collection<String> = emptyList(),
    @Serializable(with = UUIDSerializer::class)
    override val byUser: PlatformUUID? = null,
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
data class FavouriteDownloadQueueEntry(
    val tdnFavoriteType: TidalFavType,
    @Serializable(with = UUIDSerializer::class)
    override val byUser: PlatformUUID? = null,
    override val type: Type? = null,
    @Transient
    override val maxRetries: Int = 5,
    @Transient
    override val callback: suspend () -> Unit = {}
) : DownloadQueueEntry()

@Serializable
data class FinishedDownloadQueueEntry(
    val downloadQueueEntry: DownloadQueueEntry,
    var result: ProcessExecutionResult,
    val logs: List<String>,
)

@Serializable
data class LogLine(
    val queueEntry: DownloadQueueEntry,
    val line: String?,
)

@Serializable
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
data class TidalSong(
    val id: String,
    val title: String,
    val artists: List<String>,
    val cover: Map<Int, String>
)
