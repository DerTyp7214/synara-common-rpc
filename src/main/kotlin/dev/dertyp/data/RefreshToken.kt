package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class RefreshToken(
    val id: @Contextual UUID,
    val tokenHash: String,
    val isRevoked: Boolean,
    val userId: @Contextual UUID,
    @Serializable(with = DateSerializer::class)
    val expiresAt: Date,
)
