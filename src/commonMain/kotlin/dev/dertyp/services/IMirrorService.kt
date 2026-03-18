@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class ImageStreamItem(val id: PlatformUUID, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImageStreamItem) return false
        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@Rpc
interface IMirrorService {
    fun getSongs(): Flow<Song>
    fun getArtists(): Flow<Artist>
    fun getArtistAliases(): Flow<ArtistAlias>
    fun getArtistSplitAliases(): Flow<ArtistSplitAlias>
    fun getAlbums(): Flow<Album>
    fun getPlaylists(): Flow<Playlist>
    fun getUserPlaylists(): Flow<UserPlaylist>
    fun getImages(): Flow<ImageStreamItem>
    fun getSongData(songId: PlatformUUID, quality: Int = 0): Flow<ByteArray>
}
