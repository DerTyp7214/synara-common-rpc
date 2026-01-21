@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
data class User(
    val id: UUID,
    val username: String,
    val passwordHash: String,
)

@Serializable
data class UserInfo(
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