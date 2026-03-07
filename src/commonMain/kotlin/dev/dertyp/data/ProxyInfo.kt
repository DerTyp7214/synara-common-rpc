package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class ProxyInfo(
    val host: String,
    val controlPort: Int,
    val id: String?
)
