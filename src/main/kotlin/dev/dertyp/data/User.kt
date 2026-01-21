package dev.dertyp.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(
    @Contextual
    val id: UUID,
    val username: String,
    val passwordHash: String,
)

@Serializable
data class UserInfo(
    @Contextual
    val id: UUID,
    val username: String,
) {
    companion object {
        fun fromUser(user: User): UserInfo {
            return UserInfo(
                id = user.id,
                username = user.username,
            )
        }
    }
}