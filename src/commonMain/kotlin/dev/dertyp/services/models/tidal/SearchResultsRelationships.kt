package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultsRelationships(
    val albums: MultiRelationshipDataDocument? = null,
    val artists: MultiRelationshipDataDocument? = null,
    val playlists: MultiRelationshipDataDocument? = null,
    val topHits: MultiRelationshipDataDocument? = null,
    val tracks: MultiRelationshipDataDocument? = null,
    val videos: MultiRelationshipDataDocument? = null
): BaseRelationships