@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
data class Session(
    val id: UUID,
    val userAgent: String,
    val ipAddress: String,
    val lastActive: Long,
    val isActive: Boolean
)