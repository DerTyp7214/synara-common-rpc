package dev.dertyp.data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class User(
    val id: @Contextual UUID,
    val username: String,
    val passwordHash: String,
)

@Serializable
data class UserInfo(
    val id: @Contextual UUID,
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