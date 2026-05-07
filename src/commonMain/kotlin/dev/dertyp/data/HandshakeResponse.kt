package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Response for the server handshake/reachability test.")
data class HandshakeResponse(
    @FieldDoc("Whether the current connection is secure (HTTPS/WSS).")
    val secure: Boolean,
    @FieldDoc("Whether the server supports WebSocket Secure connections.")
    val wssSupported: Boolean
)
