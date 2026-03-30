package dev.dertyp.services.models.tidal

import kotlinx.serialization.Serializable

@Serializable
data class IncludedInner<A : AttributeType, R : BaseRelationships>(
    val id: String,
    val type: String,
    val attributes: A? = null,
    val relationships: R? = null,
)