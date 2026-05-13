@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.AuthenticationRequest
import dev.dertyp.data.RequiresAdmin
import dev.dertyp.data.User
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Manages user profiles and identities.")
interface IUserService {
    @RpcDoc("Look up a user by their unique ID.")
    suspend fun findUserById(
        @RpcParamDoc("The unique UUID of the user.") id: PlatformUUID
    ): User?
    @RpcDoc("Look up a user by their username.")
    suspend fun findUserByUsername(
        @RpcParamDoc("The username of the user.") username: String
    ): User?
    @RestGet
    @RpcDoc("Get the profile of the current authenticated user.")
    suspend fun me(): User
    @RequiresAdmin
    @RpcDoc("List all users on the server.", errors = ["IllegalStateException"])
    suspend fun getAllUsers(): List<User>
    @RpcDoc("Update the current user's avatar.")
    suspend fun setProfileImage(
        @RpcParamDoc("The raw bytes of the image.") bytes: ByteArray
    )
    @RpcDoc("Update the current user's display name.")
    suspend fun setDisplayName(
        @RpcParamDoc("The new display name.") name: String?
    )
    @RequiresAdmin
    @RpcDoc("Update the capabilities for a specific user.")
    suspend fun setCapabilities(
        @RpcParamDoc("The UUID of the user.") id: PlatformUUID,
        @RpcParamDoc("The new list of capabilities.") capabilities: List<UserCapability>
    )
    @RestPost
    @RequiresAdmin
    @RpcDoc("Create a new user.")
    suspend fun createUser(
        @RpcParamDoc("The user data.") user: AuthenticationRequest,
        @RpcParamDoc("Whether the user is an admin.") isAdmin: Boolean = false,
        @RpcParamDoc("The initial capabilities of the user.") capabilities: List<UserCapability> = emptyList()
    ): User?
}
