package dev.dertyp.services.metadata

import dev.dertyp.serializers.DurationSerializer
import dev.dertyp.serializers.LocalDateSerializer
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.time.Duration

interface IMetadataService {

    @Serializable
    data class AccessTokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("token_type")
        val tokenType: String,
        @SerialName("expires_in")
        val expiresIn: Int,
    ) : IMetadataService

    @Serializable
    data class Image(
        val url: String,
        val width: Int,
        val height: Int,
        val animated: Boolean = url.endsWith(".mp4")
    )

    @Serializable
    sealed class BaseMetadata

    @Serializable
    data class Artist(
        val id: String,
        val name: String,
        val popularity: Float,
        val url: String? = null,
        val images: List<Image>,
    ) : BaseMetadata()

    @Serializable
    data class Track(
        val id: String,
        val title: String,
        val artists: List<String> = emptyList(),
        @Serializable(with = DurationSerializer::class)
        val duration: Duration,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val createdAt: OffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val addedAt: OffsetDateTime? = null,
        val trackNumber: Int? = null,
        val discNumber: Int? = null,
        val images: List<Image>,
    ) : BaseMetadata() {
        val cover: String?
            get() = images.maxByOrNull { it.width }?.url
    }

    @Serializable
    data class Album(
        val id: String,
        val title: String,
        val artists: List<String> = emptyList(),
        @Transient
        val tracks: Flow<Track> = emptyFlow(),
        @Serializable(with = DurationSerializer::class)
        val duration: Duration = Duration.ZERO,
        val trackCount: Int = 0,
        val discCount: Int = 0,
        @Serializable(with = LocalDateSerializer::class)
        val releaseDate: LocalDate? = null,
        val images: List<Image> = emptyList(),
    ) : BaseMetadata()

    @Serializable
    data class Mix(
        val id: String,
        val name: String,
        @Transient
        val tracks: Flow<Track> = emptyFlow(),
        val images: List<Image>,
    ) : BaseMetadata()

    @Serializable
    data class Playlist(
        val id: String,
        val name: String,
        val description: String,
        val tracks: List<Track> = emptyList(),
        val trackCount: Int = tracks.size,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val createdAt: OffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val modifiedAt: OffsetDateTime? = null,
        val images: List<Image>,
    ) : BaseMetadata()

    @Serializable
    data class FlowPlaylist(
        val id: String,
        val name: String,
        val description: String,
        @Transient
        val tracks: Flow<Track> = emptyFlow(),
        val trackCount: Int = 0,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val createdAt: OffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val modifiedAt: OffsetDateTime? = null,
        val images: List<Image>,
    ) : BaseMetadata() {
        private val cache = mutableListOf<Track>()
        private var collectionJob: Deferred<List<Track>>? = null

        private fun getOrStartCollection(): Deferred<List<Track>> {
            return synchronized(this) {
                collectionJob ?: CoroutineScope(Dispatchers.IO).async {
                    tracks.onEach { track ->
                        synchronized(cache) { cache.add(track) }
                    }.toList()
                }.also { collectionJob = it }
            }
        }

        val sharedTracks: Flow<Track> = flow {
            val job = getOrStartCollection()
            val currentCache = synchronized(cache) { cache.toList() }
            currentCache.forEach { track -> emit(track) }
            tracks.drop(currentCache.size).collect { track ->
                emit(track)
            }
            job.join()
        }

        suspend fun collect(): Playlist {
            return Playlist(
                id = id,
                name = name,
                description = description,
                tracks = getOrStartCollection().await(),
                trackCount = trackCount,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                images = images,
            )
        }

        companion object {
            fun fromPlaylist(playlist: Playlist): FlowPlaylist {
                return FlowPlaylist(
                    id = playlist.id,
                    name = playlist.name,
                    description = playlist.description,
                    tracks = playlist.tracks.asFlow(),
                    trackCount = playlist.tracks.size,
                    createdAt = playlist.createdAt,
                    modifiedAt = playlist.modifiedAt,
                    images = playlist.images,
                )
            }
        }
    }
}