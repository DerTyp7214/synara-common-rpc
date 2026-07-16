package dev.dertyp.services

import dev.dertyp.data.SubsonicCredentialInfo
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manages the current user's dedicated Subsonic credential used by Subsonic/OpenSubsonic clients.")
interface ISubsonicCredentialService {
    @RpcDoc("Get the current user's Subsonic credential, or null if none exists.")
    suspend fun getSubsonicCredential(): SubsonicCredentialInfo?

    @RpcDoc("Generate a new Subsonic credential for the current user, replacing any existing one.")
    suspend fun regenerateSubsonicCredential(): SubsonicCredentialInfo

    @RpcDoc("Delete the current user's Subsonic credential. Returns true if a credential was deleted.")
    suspend fun revokeSubsonicCredential(): Boolean
}
