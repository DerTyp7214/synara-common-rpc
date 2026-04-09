package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Information about a connected reverse proxy instance.")
data class ProxyInfo(
    @FieldDoc("The hostname or IP of the proxy.")
    val host: String,
    @FieldDoc("The port used for the control connection.")
    val controlPort: Int,
    @FieldDoc("Whether the proxy uses SSL/TLS.")
    val ssl: Boolean,
    @FieldDoc("The unique identifier of the proxy instance.")
    val id: String?
)
