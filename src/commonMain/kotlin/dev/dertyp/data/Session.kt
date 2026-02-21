@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class Session(
    val id: PlatformUUID,
    val userAgent: String,
    val ipAddress: String,
    val lastActive: Long,
    val isActive: Boolean
)
