package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val userAgent: String,
    val ipAddress: String,
    val lastActive: Long,
    val isActive: Boolean
)