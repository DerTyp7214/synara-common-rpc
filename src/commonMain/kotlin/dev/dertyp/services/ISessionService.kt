package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Session
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manages active user sessions and connected devices.")
interface ISessionService {
    @RpcDoc("Terminate a specific user session.")
    suspend fun deactivateSession(
        @RpcParamDoc("The session unique identifier.") sessionId: PlatformUUID
    )
    @RpcDoc("List all sessions for the current user.")
    suspend fun getSessions(): List<Session>
}