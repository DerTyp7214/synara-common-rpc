package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Request payload for refreshing an authentication token.")
data class RefreshTokenRequest(
    @FieldDoc("The refresh token.")
    val refreshToken: String
)

@Serializable
@ModelDoc("Request payload for user authentication.")
data class AuthenticationRequest(
    @FieldDoc("The username.")
    val username: String,
    @FieldDoc("The plain text password.")
    val password: String,
)

@Serializable
@ModelDoc("Response payload containing JWT tokens after successful authentication.")
data class AuthenticationResponse(
    @FieldDoc("The primary JWT access token.")
    val token: String,
    @FieldDoc("A long-lived token used to obtain new access tokens.")
    val refreshToken: String,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of when the access token expires.")
    val expiresAt: PlatformDate,
)
