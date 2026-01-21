@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
data class RefreshToken(
    val id: UUID,
    val tokenHash: String,
    val isRevoked: Boolean,
    val userId: UUID,
    @Serializable(with = DateSerializer::class)
    val expiresAt: Date,
)
