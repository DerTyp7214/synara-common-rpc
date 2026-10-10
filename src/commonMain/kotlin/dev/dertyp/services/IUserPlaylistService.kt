@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ArtistPlaylistSortStrategy
import dev.dertyp.data.InsertablePlaylist
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.PlaylistAccess
import dev.dertyp.data.UserPlaylist
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPath
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Management of personal (user-created) playlists.")
interface IUserPlaylistService {
    @RpcDoc("Get user playlist by ID.")
    suspend fun byId(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): UserPlaylist?

    @RpcDoc("Get multiple user playlists by their IDs.")
    suspend fun byIds(@RpcParamDoc("Collection of playlist IDs.") ids: List<PlatformUUID>): List<UserPlaylist>

    @RpcDoc("Search user playlists.")
    suspend fun rankedSearch(
        @RpcParamDoc("Optional creator ID to filter by.") creator: PlatformUUID?,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The search query.") query: String
    ): PaginatedResponse<UserPlaylist>

    @RpcDoc("Get all user playlists.")
    suspend fun allPlaylists(
        @RpcParamDoc("Optional creator ID to filter by.") creator: PlatformUUID?,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<UserPlaylist>

    @RpcDoc("Search for user playlists by color.")
    suspend fun byColor(
        @RpcParamDoc("Optional creator ID to filter by.") creator: PlatformUUID?,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50,
        @RpcParamDoc("The target color in ARGB format.") color: Int,
        @RpcParamDoc("The allowed range (0-255).") range: Int = 20
    ): PaginatedResponse<UserPlaylist>

    @RestPath("playlist")
    @RpcDoc("Delete a user playlist.", errors = ["UnauthorizedException"])
    suspend fun delete(@RpcParamDoc("The playlist unique identifier.") id: PlatformUUID): Boolean

    @RestPost
    @RpcDoc(
        "Create a new user playlist or retrieve an existing one by a custom identifier.",
        errors = ["UnauthorizedException"]
    )
    suspend fun getOrAddPlaylist(
        @RpcParamDoc("The user ID who owns the playlist.") userId: PlatformUUID,
        @RpcParamDoc("Optional unique string identifier from an external source.") customIdentifier: String?,
        @RpcParamDoc("The initial playlist data.") playlist: InsertablePlaylist
    ): PlatformUUID

    @RpcDoc("Add songs to a user playlist.", errors = ["UnauthorizedException"])
    suspend fun addToPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of song IDs and their added timestamps.") songIds: List<Pair<Long, PlatformUUID>>
    )

    @RpcDoc("Add songs to a user playlist.", errors = ["UnauthorizedException"])
    suspend fun addSongsToPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of song IDs to add.") songIds: List<PlatformUUID>
    )

    @RpcDoc("Add all songs of an album to a user playlist.", errors = ["UnauthorizedException"])
    suspend fun addAlbumToPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The album unique identifier.") albumId: PlatformUUID
    )

    @RpcDoc("Add all songs of a playlist to a user playlist.", errors = ["UnauthorizedException"])
    suspend fun addPlaylistToPlaylist(
        @RpcParamDoc("The target playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The source playlist unique identifier.") sourcePlaylistId: PlatformUUID
    )

    @RpcDoc("Add all songs of a user playlist to a user playlist.", errors = ["UnauthorizedException"])
    suspend fun addUserPlaylistToPlaylist(
        @RpcParamDoc("The target playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The source user playlist unique identifier.") sourcePlaylistId: PlatformUUID
    )

    @RestPath("songs")
    @RpcDoc("Remove songs from a user playlist.", errors = ["UnauthorizedException"])
    suspend fun removeFromPlaylist(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Collection of song IDs to remove.") songIds: List<PlatformUUID>
    ): Int

    @RpcDoc("Set the cover image for a user playlist.", errors = ["UnauthorizedException"])
    suspend fun setPlaylistImage(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The image unique identifier.") imageId: PlatformUUID?
    ): Boolean

    @RpcDoc("Create a smart playlist based on artists.", errors = ["UnauthorizedException"])
    suspend fun createPlaylistFromArtists(
        @RpcParamDoc("The user ID who owns the playlist.") userId: PlatformUUID,
        @RpcParamDoc("The name of the playlist.") name: String,
        @RpcParamDoc("Collection of artist IDs.") artistIds: List<PlatformUUID>,
        @RpcParamDoc("Maximum number of songs per artist.") maxSongsPerArtist: Int = 10,
        @RpcParamDoc("Sorting strategy.") sortStrategy: ArtistPlaylistSortStrategy = ArtistPlaylistSortStrategy.MB_RELEASE_DATE
    ): PlatformUUID

    @RpcDoc(
        "Make a user playlist public or private. A public playlist can be found and opened by every user, a private one only by its owner and the users it is shared with. Returns false when the playlist does not exist.",
        errors = ["UnauthorizedException"]
    )
    suspend fun setPublic(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("Whether the playlist is public.") isPublic: Boolean
    ): Boolean

    @RpcDoc(
        "Share a user playlist with a user, or change the access of a user it is already shared with. Only the owner can do this, and the owner cannot be given a share.",
        errors = ["IllegalArgumentException", "UnauthorizedException"]
    )
    suspend fun setShare(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The unique identifier of the user to share the playlist with.") userId: PlatformUUID,
        @RpcParamDoc("What the user may do with the playlist.") access: PlaylistAccess
    )

    @RpcDoc(
        "Stop sharing a user playlist with a user. The owner can remove any share, and a user can remove their own to leave the playlist. Returns false when the playlist was not shared with that user.",
        errors = ["UnauthorizedException"]
    )
    suspend fun removeShare(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The unique identifier of the user to remove.") userId: PlatformUUID
    ): Boolean

    @RestGet
    @RpcDoc("Get the user playlists that other users shared with the current user.")
    suspend fun sharedPlaylists(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 50
    ): PaginatedResponse<UserPlaylist>

    @RpcDoc(
        "Hand a user playlist over to another user. The previous owner keeps write access through a share. Returns false when the playlist or the new owner does not exist or the user already owns the playlist.",
        errors = ["UnauthorizedException"]
    )
    suspend fun transferOwnership(
        @RpcParamDoc("The playlist unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The unique identifier of the user who becomes the owner.") newOwnerId: PlatformUUID
    ): Boolean
}
