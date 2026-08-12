package dev.dertyp.services

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Handles user login and session security.")
interface IAuthService {
    @RpcDoc("Logs in a user and returns JWT token.", errors = ["IllegalArgumentException", "IllegalStateException"])
    suspend fun authenticate(
        @RpcParamDoc("The username of the user.") username: String,
        @RpcParamDoc("The password of the user.") password: String
    ): AuthenticationResponse
    @RpcDoc("Refreshes an expired access token.", errors = ["IllegalArgumentException", "IllegalStateException"])
    suspend fun refreshToken(
        @RpcParamDoc("The refresh token.") refreshToken: String
    ): AuthenticationResponse
    @RpcDoc(
        "Creates a new session for another device (e.g. Apple TV) on behalf of the authenticated user.",
        errors = ["IllegalStateException"]
    )
    suspend fun createDeviceSession(
        @RpcParamDoc("The user agent of the device the session is created for.") userAgent: String
    ): AuthenticationResponse
}