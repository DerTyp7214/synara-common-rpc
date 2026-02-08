package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomMetadata(
    val title: String? = null,
    val artists: List<String>? = null,
    val album: String? = null,
    val year: String? = null,
    val genre: String? = null
)
