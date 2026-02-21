@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class RefreshToken(
    val id: PlatformUUID,
    val tokenHash: String,
    val isRevoked: Boolean,
    val userId: PlatformUUID,
    @Serializable(with = DateSerializer::class)
    val expiresAt: PlatformDate,
)
