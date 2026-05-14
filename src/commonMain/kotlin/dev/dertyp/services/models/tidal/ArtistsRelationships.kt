package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsRelationships(
    val albums: MultiRelationshipDataDocument? = null,
    val biography: SingleRelationshipDataDocument? = null,
    val followers: ArtistsFollowersMultiRelationshipDataDocument<ArtistsAttributes, ArtistsRelationships>? = null,
    val following: ArtistsFollowingMultiRelationshipDataDocument<ArtistsAttributes, ArtistsRelationships>? = null,
    val owners: MultiRelationshipDataDocument? = null,
    val profileArt: MultiRelationshipDataDocument? = null,
    val radio: MultiRelationshipDataDocument? = null,
    val roles: MultiRelationshipDataDocument? = null,
    val similarArtists: MultiRelationshipDataDocument? = null,
    val trackProviders: ArtistsTrackProvidersMultiRelationshipDataDocument<ArtistsAttributes, ArtistsRelationships>? = null,
    val tracks: MultiRelationshipDataDocument? = null,
    val videos: MultiRelationshipDataDocument? = null
): BaseRelationships