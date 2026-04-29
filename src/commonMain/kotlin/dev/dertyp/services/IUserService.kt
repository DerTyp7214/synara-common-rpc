@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.User
import dev.dertyp.rpc.annotations.RestGet
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
    @RpcDoc("List all users on the server.", adminOnly = true, errors = ["IllegalStateException"])
    suspend fun getAllUsers(): List<User>
    @RpcDoc("Update the current user's avatar.")
    suspend fun setProfileImage(
        @RpcParamDoc("The raw bytes of the image.") bytes: ByteArray
    )
    @RpcDoc("Update the current user's display name.")
    suspend fun setDisplayName(
        @RpcParamDoc("The new display name.") name: String?
    )
}
