package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import dev.dertyp.serializers.UUIDByteSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RefreshToken(
    @Serializable(with = UUIDByteSerializer::class)
    val id: UUID,
    val tokenHash: String,
    val isRevoked: Boolean,
    @Serializable(with = UUIDByteSerializer::class)
    val userId: UUID,
    @Serializable(with = DateSerializer::class)
    val expiresAt: Date,
)
