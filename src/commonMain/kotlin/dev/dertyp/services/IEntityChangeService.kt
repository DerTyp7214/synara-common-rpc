package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.EntityChange
import dev.dertyp.data.EntityChangeWindow
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Answers which library entities changed since a time the client names, so a client that keeps library data reads only those again " +
        "instead of everything. It is a pull the client starts and not a subscription. Every returned stream delivers the changes recorded " +
        "so far and then completes, nothing is emitted afterwards. The live notifications are @IChangeService.observeChanges. " +
        "A result contains library changes plus the caller's own LIKE and TIMECODES changes, ordered by time. " +
        "A filtered pull returns the changes of every object a client gets when it loads that artist, album, playlist or collection, " +
        "including objects nested in others, such as the artists and the album of a song, the artists of an album and the member artists " +
        "of an artist that is a group. A change is recorded once for the entity it happened to, so an object nested in several others is " +
        "reported once as itself, and a client updates the copies it holds by id. " +
        "A client calls @IEntityChangeService.getWindow first, pulls with the time it kept from its last pull and keeps the new one for the next."
)
interface IEntityChangeService {
    @RpcDoc(
        "Read the time frame around a pull. The client keeps serverTime as the since of its next pull and compares the since it is about " +
            "to use with availableSince. If that since lies before availableSince the recorded changes no longer reach back far enough " +
            "and the client reads everything again."
    )
    suspend fun getWindow(): EntityChangeWindow

    @RpcDoc("Stream every recorded change since a time, ordered by time. The stream completes after the last entry.")
    fun allChanges(
        @RpcParamDoc("The time to read from (epoch milliseconds). Entries changed at or after it are returned.") since: Long
    ): Flow<EntityChange>

    @RpcDoc(
        "Stream the recorded changes of everything a client gets when it loads an artist since a time, ordered by time. These are the changes " +
            "of the artist itself, of its member artists when it is a group, of its albums and songs, deleted ones included, of the albums " +
            "of those songs, of every artist credited on those albums and songs, and of the member artists of credited groups. " +
            "The caller's LIKE changes of these songs, albums and artists and its TIMECODES changes of these songs are part of it. " +
            "The stream completes after the last entry."
    )
    fun byArtist(
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("The time to read from (epoch milliseconds). Entries changed at or after it are returned.") since: Long
    ): Flow<EntityChange>

    @RpcDoc(
        "Stream the recorded changes of everything a client gets when it loads an album since a time, ordered by time. These are the changes " +
            "of the album itself, of its artists, of its songs, deleted ones included, of every artist credited on those songs, and of " +
            "the member artists of credited groups. The caller's LIKE changes of the album, these songs and these artists and its " +
            "TIMECODES changes of these songs are part of it. The stream completes after the last entry."
    )
    fun byAlbum(
        @RpcParamDoc("The album unique identifier.") albumId: PlatformUUID,
        @RpcParamDoc("The time to read from (epoch milliseconds). Entries changed at or after it are returned.") since: Long
    ): Flow<EntityChange>

    @RpcDoc(
        "Stream the recorded changes of everything a client gets when it loads a playlist since a time, ordered by time. These are the changes " +
            "of the playlist itself, its contents changing included, of the songs it contains now, of the albums of those songs, of every " +
            "artist credited on those songs and albums, and of the member artists of credited groups. The caller's LIKE changes of these " +
            "songs, albums and artists and its TIMECODES changes of these songs are part of it. It works for user playlists and global " +
            "playlists alike. The stream completes after the last entry."
    )
    fun byPlaylist(
        @RpcParamDoc("The unique identifier of the user playlist or global playlist.") playlistId: PlatformUUID,
        @RpcParamDoc("The time to read from (epoch milliseconds). Entries changed at or after it are returned.") since: Long
    ): Flow<EntityChange>

    @RpcDoc(
        "Stream the recorded changes of everything a client gets when it loads a collection since a time, ordered by time. These are the " +
            "changes of the collection itself and of its direct members, which are songs, albums, artists and user playlists. " +
            "The members are expanded to what they contain. A member album brings its songs, a member artist brings its albums, its songs " +
            "and the songs of those albums, and a member playlist brings the songs it contains now. For every song reached that way the " +
            "changes of its album and of its credited artists are returned as well, for every album those of its artists, and for every " +
            "such artist that is a group those of its member artists. The caller's LIKE changes of these songs, albums and artists and " +
            "its TIMECODES changes of these songs are part of it. The stream completes after the last entry."
    )
    fun byCollection(
        @RpcParamDoc("The collection unique identifier.") collectionId: PlatformUUID,
        @RpcParamDoc("The time to read from (epoch milliseconds). Entries changed at or after it are returned.") since: Long
    ): Flow<EntityChange>
}
