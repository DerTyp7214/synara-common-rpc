package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class AlbumsRelationships<A : BaseAttributes, R : BaseRelationships>(
    val artists: MultiRelationshipDataDocument? = null,
    val coverArt: MultiRelationshipDataDocument? = null,
    val genres: MultiRelationshipDataDocument? = null,
    val items: AlbumsItemsMultiRelationshipDataDocument<A, R>? = null,
    val owners: MultiRelationshipDataDocument? = null,
    val providers: MultiRelationshipDataDocument? = null,
    val similarAlbums: MultiRelationshipDataDocument? = null
): BaseRelationships
