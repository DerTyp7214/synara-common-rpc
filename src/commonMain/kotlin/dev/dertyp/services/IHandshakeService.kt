package dev.dertyp.services

import dev.dertyp.data.HandshakeResponse
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPublic
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Service for server reachability and handshake testing.")
interface IHandshakeService {
    @RestGet
    @RestPublic
    @RpcDoc("Perform a handshake test to check server reachability and protocol support.")
    suspend fun handshake(): HandshakeResponse
}
