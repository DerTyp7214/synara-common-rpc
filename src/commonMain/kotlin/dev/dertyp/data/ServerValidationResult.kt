package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class ServerValidationResult(
    val validated: Boolean,
    val useSsl: Boolean
)
