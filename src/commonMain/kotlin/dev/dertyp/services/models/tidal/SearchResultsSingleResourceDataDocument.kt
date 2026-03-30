package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultsSingleResourceDataDocument<A : AttributeType, R : BaseRelationships>(
    val data: SearchResultsResourceObject,
    val links: Links,
    val included: List<IncludedInner<A, R>>
)