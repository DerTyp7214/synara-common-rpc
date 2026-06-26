@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("The kind of entity a collection item refers to.")
enum class CollectionItemType {
    @FieldDoc("A single song.")
    SONG,
    @FieldDoc("A whole album.")
    ALBUM,
    @FieldDoc("A whole artist.")
    ARTIST,
    @FieldDoc("A user-created playlist.")
    PLAYLIST
}

@Serializable
@ModelDoc("A user-owned, downloadable grouping of songs, albums, artists and playlists.")
data class MediaCollection(
    @FieldDoc("The collection unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The name of the collection.")
    val name: String,
    @FieldDoc("An optional user-provided description.")
    @property:ObjCName("collectionDescription") val description: String? = null,
    @FieldDoc("The optional cover image unique identifier.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the cover image.")
    val blurHash: String? = null,
    @FieldDoc("The unique identifier of the user who created the collection.")
    val creator: PlatformUUID,
    @FieldDoc("Total size in bytes of all distinct songs the collection resolves to.")
    val totalSizeBytes: Long = 0L,
    @FieldDoc("Number of distinct songs the collection resolves to.")
    val songCount: Int = 0,
    @FieldDoc("Number of songs explicitly added as items.")
    val songItemCount: Int = 0,
    @FieldDoc("Number of albums explicitly added as items.")
    val albumCount: Int = 0,
    @FieldDoc("Number of artists explicitly added as items.")
    val artistCount: Int = 0,
    @FieldDoc("Number of playlists explicitly added as items.")
    val playlistCount: Int = 0,
)

@Serializable
@ModelDoc("Configuration for creating or updating a collection.")
data class InsertableCollection(
    @FieldDoc("The name of the collection.")
    val name: String,
    @FieldDoc("An optional user-provided description.")
    @property:ObjCName("collectionDescription") val description: String? = null,
    @FieldDoc("The optional cover image unique identifier.")
    val imageId: PlatformUUID? = null,
)
