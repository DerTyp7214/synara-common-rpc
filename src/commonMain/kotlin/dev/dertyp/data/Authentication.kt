package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class AuthenticationRequest(
    val username: String,
    val password: String,
)

@Serializable
data class AuthenticationResponse(
    val token: String,
    val refreshToken: String,
    @Serializable(with = DateSerializer::class)
    val expiresAt: PlatformDate,
)
