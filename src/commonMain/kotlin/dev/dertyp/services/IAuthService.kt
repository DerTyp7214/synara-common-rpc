package dev.dertyp.services

import dev.dertyp.data.AuthenticationResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IAuthService {
    suspend fun authenticate(username: String, password: String): AuthenticationResponse
    suspend fun refreshToken(refreshToken: String): AuthenticationResponse
}