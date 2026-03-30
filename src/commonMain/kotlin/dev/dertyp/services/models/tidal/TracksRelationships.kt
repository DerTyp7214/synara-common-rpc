package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class TracksRelationships(
    val albums: MultiRelationshipDataDocument? = null,
    val artists: MultiRelationshipDataDocument? = null,
    val genres: MultiRelationshipDataDocument? = null,
    val lyrics: MultiRelationshipDataDocument? = null,
    val owners: MultiRelationshipDataDocument? = null,
    val providers: MultiRelationshipDataDocument? = null,
    val radio: MultiRelationshipDataDocument? = null,
    val shares: MultiRelationshipDataDocument? = null,
    val similarTracks: MultiRelationshipDataDocument? = null,
    val sourceFile: SingleRelationshipDataDocument? = null,
    val trackStatistics: SingleRelationshipDataDocument? = null
): BaseRelationships
