package dev.dertyp.services.metadata

import dev.dertyp.PlatformLocalDate
import dev.dertyp.PlatformOffsetDateTime
import dev.dertyp.ioDispatcher
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DurationSerializer
import dev.dertyp.serializers.LocalDateSerializer
import dev.dertyp.serializers.OffsetDateTimeSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration

@ModelDoc("Generic interface for services providing external media metadata.")
interface IMetadataService {

    @Serializable
    @ModelDoc("Response containing an access token for an external metadata service.")
    data class AccessTokenResponse(
        @SerialName("access_token")
        @FieldDoc("The access token string.")
        val accessToken: String,
        @SerialName("token_type")
        @FieldDoc("The type of the token (e.g., Bearer).")
        val tokenType: String,
        @SerialName("expires_in")
        @FieldDoc("Token lifetime in seconds.")
        val expiresIn: Int,
    ) : IMetadataService

    @Serializable
    @ModelDoc("Metadata for an image from an external source.")
    data class Image(
        @FieldDoc("The URL of the image.")
        val url: String,
        @FieldDoc("Width in pixels.")
        val width: Int,
        @FieldDoc("Height in pixels.")
        val height: Int,
        @FieldDoc("Whether the image is animated.")
        val animated: Boolean = url.endsWith(".mp4")
    )

    @Serializable
    @ModelDoc("Base class for all external metadata entities.")
    sealed class BaseMetadata

    @Serializable
    @ModelDoc("External metadata for a music artist.")
    data class Artist(
        @FieldDoc("The external artist ID.")
        val id: String,
        @FieldDoc("The name of the artist.")
        val name: String,
        @FieldDoc("Popularity score of the artist.")
        val popularity: Float,
        @FieldDoc("The URL to the artist's profile.")
        val url: String? = null,
        @FieldDoc("Collection of artist images.")
        val images: List<Image>,
        @FieldDoc("Biography or description text.")
        val biography: String? = null,
        @FieldDoc("Musical styles associated with the artist.")
        val styles: List<String> = emptyList(),
        @FieldDoc("Genres associated with the artist.")
        val genres: List<String> = emptyList(),
    ) : BaseMetadata()

    @Serializable
    @ModelDoc("External metadata for a music track.")
    data class Track(
        @FieldDoc("The external track ID.")
        val id: String,
        @FieldDoc("The title of the track.")
        val title: String,
        @FieldDoc("Collection of artist names.")
        val artists: List<String> = emptyList(),
        @Serializable(with = DurationSerializer::class)
        @FieldDoc("Duration of the track.")
        val duration: Duration,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of when the track was created on the source platform.")
        val createdAt: PlatformOffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of when the track was added to the source collection.")
        val addedAt: PlatformOffsetDateTime? = null,
        @FieldDoc("Track position in the album.")
        val trackNumber: Int? = null,
        @FieldDoc("Disc number in the album.")
        val discNumber: Int? = null,
        @FieldDoc("Collection of track images.")
        val images: List<Image>,
        @FieldDoc("Genres associated with the track.")
        val genres: List<String> = emptyList(),
    ) : BaseMetadata()

    @Serializable
    @ModelDoc("External metadata for a music album.")
    data class Album(
        @FieldDoc("The external album ID.")
        val id: String,
        @FieldDoc("The title of the album.")
        val title: String,
        @FieldDoc("Collection of artist names.")
        val artists: List<String> = emptyList(),
        @Transient
        @FieldDoc("Stream of tracks included in the album.")
        val tracks: Flow<Track> = emptyFlow(),
        @Serializable(with = DurationSerializer::class)
        @FieldDoc("Total duration of the album.")
        val duration: Duration = Duration.ZERO,
        @FieldDoc("Number of tracks in the album.")
        val trackCount: Int = 0,
        @FieldDoc("Number of discs in the album.")
        val discCount: Int = 0,
        @Serializable(with = LocalDateSerializer::class)
        @FieldDoc("The date the album was released.")
        val releaseDate: PlatformLocalDate? = null,
        @FieldDoc("Collection of album images.")
        val images: List<Image> = emptyList(),
        @FieldDoc("Genres associated with the album.")
        val genres: List<String> = emptyList(),
        @FieldDoc("Alternative titles for the album.")
        val additionalTitles: List<String> = emptyList()
    ) : BaseMetadata()

    @Serializable
    @ModelDoc("External metadata for a curated music mix.")
    data class Mix(
        @FieldDoc("The external mix ID.")
        val id: String,
        @FieldDoc("The name of the mix.")
        val name: String,
        @Transient
        @FieldDoc("Stream of tracks included in the mix.")
        val tracks: Flow<Track> = emptyFlow(),
        @FieldDoc("Collection of mix images.")
        val images: List<Image>,
    ) : BaseMetadata()

    @Serializable
    @ModelDoc("External metadata for a music playlist.")
    data class Playlist(
        @FieldDoc("The external playlist ID.")
        val id: String,
        @FieldDoc("The name of the playlist.")
        val name: String,
        @FieldDoc("Description of the playlist.")
        val description: String,
        @FieldDoc("List of tracks in the playlist.")
        val tracks: List<Track> = emptyList(),
        @FieldDoc("Number of tracks in the playlist.")
        val trackCount: Int = tracks.size,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of when the playlist was created.")
        val createdAt: PlatformOffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of the last modification.")
        val modifiedAt: PlatformOffsetDateTime? = null,
        @FieldDoc("Collection of playlist images.")
        val images: List<Image>,
    ) : BaseMetadata()

    @Serializable
    @ModelDoc("Reactive flow-based external playlist metadata.")
    data class FlowPlaylist(
        @FieldDoc("The external playlist ID.")
        val id: String,
        @FieldDoc("The name of the playlist.")
        val name: String,
        @FieldDoc("Description of the playlist.")
        val description: String,
        @Transient
        @FieldDoc("Stream of tracks in the playlist.")
        val tracks: Flow<Track> = emptyFlow(),
        @FieldDoc("Number of tracks in the playlist.")
        val trackCount: Int = 0,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of when the playlist was created.")
        val createdAt: PlatformOffsetDateTime? = null,
        @Serializable(with = OffsetDateTimeSerializer::class)
        @FieldDoc("Timestamp of the last modification.")
        val modifiedAt: PlatformOffsetDateTime? = null,
        @FieldDoc("Collection of playlist images.")
        val images: List<Image>,
    ) : BaseMetadata() {
        @Transient
        private val cache = mutableListOf<Track>()
        @Transient
        private var collectionJob: Deferred<List<Track>>? = null
        @Transient
        private val mutex = Mutex()

        private suspend fun getOrStartCollection(): Deferred<List<Track>> {
            return mutex.withLock {
                collectionJob ?: CoroutineScope(ioDispatcher).async {
                    tracks.onEach { track ->
                        mutex.withLock { cache.add(track) }
                    }.toList()
                }.also { collectionJob = it }
            }
        }

        val sharedTracks: Flow<Track> = flow {
            val job = getOrStartCollection()
            val currentCache = mutex.withLock { cache.toList() }
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

    @Serializable
    data class FlowArtist(
        val id: String,
        @Transient
        val tracks: Flow<Track> = emptyFlow(),
    ): BaseMetadata() {
        @Transient
        private val cache = mutableListOf<Track>()
        @Transient
        private var collectionJob: Deferred<List<Track>>? = null
        @Transient
        private val mutex = Mutex()

        private suspend fun getOrStartCollection(): Deferred<List<Track>> {
            return mutex.withLock {
                collectionJob ?: CoroutineScope(ioDispatcher).async {
                    tracks.onEach {
                        mutex.withLock { cache.add(it) }
                    }.toList()
                }.also { collectionJob = it }
            }
        }

        val sharedTracks: Flow<Track> = flow {
            val job = getOrStartCollection()
            val currentCache = mutex.withLock { cache.toList() }
            currentCache.forEach { track -> emit(track) }
            tracks.drop(currentCache.size).collect { track ->
                emit(track)
            }
            job.join()
        }

        suspend fun collect(): List<Track> {
            return getOrStartCollection().await()
        }
    }
}
