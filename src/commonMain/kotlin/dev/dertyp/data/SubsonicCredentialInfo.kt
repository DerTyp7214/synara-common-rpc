package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The dedicated Subsonic credential for a user. The password is retrievable because Subsonic token authentication requires the server to know it.")
data class SubsonicCredentialInfo(
    @FieldDoc("The username to use in Subsonic clients.")
    val username: String,
    @FieldDoc("The generated Subsonic password.")
    val password: String,
    @FieldDoc("Unix timestamp (ms) when the credential was created.")
    val createdAt: Long,
)
