package dev.dertyp.services

import dev.dertyp.data.User
import kotlinx.rpc.annotations.Rpc
import java.util.UUID

@Rpc
interface IUserService {
    suspend fun findUserById(id: UUID): User?
    suspend fun findUserByUsername(username: String): User?
    suspend fun me(): User
}
