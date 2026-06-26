@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.CollectionItemType
import dev.dertyp.data.MediaCollection
import dev.dertyp.data.InsertableCollection
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Management of user-owned downloadable collections.")
interface ICollectionService {
    @RpcDoc("Get a collection by ID.")
    suspend fun byId(@RpcParamDoc("The collection unique identifier.") id: PlatformUUID): MediaCollection?

    @RpcDoc("Get all collections owned by the current user.")
    suspend fun allCollections(): List<MediaCollection>

    @RpcDoc("Create a new collection owned by the current user.")
    suspend fun createCollection(
        @RpcParamDoc("The initial collection data.") collection: InsertableCollection
    ): PlatformUUID

    @RpcDoc("Update a collection's metadata (name, description, image).")
    suspend fun updateCollection(
        @RpcParamDoc("The collection unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The new metadata.") collection: InsertableCollection
    ): Boolean

    @RpcDoc("Add an item (song, album, artist or playlist) to a collection.")
    suspend fun addItem(
        @RpcParamDoc("The collection unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The kind of referenced entity.") itemType: CollectionItemType,
        @RpcParamDoc("The referenced entity unique identifier.") itemId: PlatformUUID
    ): Boolean

    @RpcDoc("Remove an item from a collection.")
    suspend fun removeItem(
        @RpcParamDoc("The collection unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The kind of referenced entity.") itemType: CollectionItemType,
        @RpcParamDoc("The referenced entity unique identifier.") itemId: PlatformUUID
    ): Boolean

    @RpcDoc("Set the cover image of a collection.")
    suspend fun setCollectionImage(
        @RpcParamDoc("The collection unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The image unique identifier.") imageId: PlatformUUID?
    ): Boolean

    @RpcDoc("Delete a collection.")
    suspend fun delete(@RpcParamDoc("The collection unique identifier.") id: PlatformUUID): Boolean

    @RestGet
    @RpcDoc("Stream the IDs of songs explicitly added to a collection.")
    fun songIds(@RpcParamDoc("The collection unique identifier.") collectionId: PlatformUUID): Flow<PlatformUUID>

    @RestGet
    @RpcDoc("Stream the IDs of albums explicitly added to a collection.")
    fun albumIds(@RpcParamDoc("The collection unique identifier.") collectionId: PlatformUUID): Flow<PlatformUUID>

    @RestGet
    @RpcDoc("Stream the IDs of artists explicitly added to a collection.")
    fun artistIds(@RpcParamDoc("The collection unique identifier.") collectionId: PlatformUUID): Flow<PlatformUUID>

    @RestGet
    @RpcDoc("Stream the IDs of playlists explicitly added to a collection.")
    fun playlistIds(@RpcParamDoc("The collection unique identifier.") collectionId: PlatformUUID): Flow<PlatformUUID>
}
