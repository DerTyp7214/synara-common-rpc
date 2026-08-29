package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Response for the server handshake/reachability test.")
data class HandshakeResponse(
    @FieldDoc("Whether the current connection is secure (HTTPS/WSS).")
    val secure: Boolean,
    @FieldDoc("Whether the server supports secure connections (HTTPS/WSS).")
    val sslSupported: Boolean,
    @FieldDoc("Highest API version the server supports. Clients send the version they support in the X-Api-Version header; absent means 1.")
    val apiVersion: Int = 1,
    @FieldDoc("Highest server-driven UI schema version the server supports; 0 means unsupported. Clients send theirs in the X-Ui-Schema-Version header.")
    val uiSchemaVersion: Int = 0
)
