@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class User(
    val id: PlatformUUID,
    val username: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val profileImageId: PlatformUUID? = null,
)

@Serializable
data class UserInfo(
    val id: PlatformUUID,
    val username: String,
    val isAdmin: Boolean,
    val profileImageId: PlatformUUID? = null,
) {
    companion object {
        fun fromUser(user: User): UserInfo {
            return UserInfo(
                id = user.id,
                username = user.username,
                isAdmin = user.isAdmin,
                profileImageId = user.profileImageId,
            )
        }
    }
}
