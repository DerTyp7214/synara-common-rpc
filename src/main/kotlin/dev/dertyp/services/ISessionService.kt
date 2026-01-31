package dev.dertyp.services

import dev.dertyp.data.Session
import kotlinx.rpc.annotations.Rpc
import java.util.*

@Rpc
interface ISessionService {
    suspend fun deactivateSession(sessionId: UUID)
    suspend fun getSessions(): List<Session>
}