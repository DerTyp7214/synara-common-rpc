package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Session
import kotlinx.rpc.annotations.Rpc

@Rpc
interface ISessionService {
    suspend fun deactivateSession(sessionId: PlatformUUID)
    suspend fun getSessions(): List<Session>
}