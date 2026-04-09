@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Contains core identity and profile data for a Synara user.")
data class User(
    @FieldDoc("The user unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The unique login name of the user.")
    val username: String,
    @FieldDoc("Optional display name shown to other users.")
    val displayName: String? = null,
    @FieldDoc("Hashed password for authentication.")
    val passwordHash: String,
    @FieldDoc("Whether the user has administrative privileges.")
    val isAdmin: Boolean = false,
    @FieldDoc("The user's profile avatar image unique identifier.")
    val profileImageId: PlatformUUID? = null,
)

@Serializable
@ModelDoc("Publicly safe profile information about a user.")
data class UserInfo(
    @FieldDoc("The user unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The unique login name of the user.")
    val username: String,
    @FieldDoc("Optional display name shown to other users.")
    val displayName: String? = null,
    @FieldDoc("Whether the user has administrative privileges.")
    val isAdmin: Boolean,
    @FieldDoc("The user's profile avatar image unique identifier.")
    val profileImageId: PlatformUUID? = null,
) {
    companion object {
        fun fromUser(user: User): UserInfo {
            return UserInfo(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                isAdmin = user.isAdmin,
                profileImageId = user.profileImageId,
            )
        }
    }
}
