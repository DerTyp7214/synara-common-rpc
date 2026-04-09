@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Represents a stored refresh token for session persistence.")
data class RefreshToken(
    @FieldDoc("The unique identifier of the refresh token.")
    val id: PlatformUUID,
    @FieldDoc("Hashed representation of the token string.")
    val tokenHash: String,
    @FieldDoc("Whether the token has been manually invalidated.")
    val isRevoked: Boolean,
    @FieldDoc("The user who owns this token.")
    val userId: PlatformUUID,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of when the token expires.")
    val expiresAt: PlatformDate,
)
