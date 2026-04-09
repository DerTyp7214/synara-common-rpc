@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Represents an active or historical user login session.")
data class Session(
    @FieldDoc("The session unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("Browser or application identifier of the connecting device.")
    val userAgent: String,
    @FieldDoc("The IP address from which the user last connected.")
    val ipAddress: String,
    @FieldDoc("Unix timestamp of the last activity in this session.")
    val lastActive: Long,
    @FieldDoc("Whether the session is currently considered active.")
    val isActive: Boolean
)
