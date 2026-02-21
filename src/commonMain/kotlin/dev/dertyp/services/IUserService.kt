package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.User
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IUserService {
    suspend fun findUserById(id: PlatformUUID): User?
    suspend fun findUserByUsername(username: String): User?
    suspend fun me(): User
}
