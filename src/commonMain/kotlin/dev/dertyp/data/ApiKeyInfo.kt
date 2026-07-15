@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Metadata about an API key. Never contains the secret itself.")
data class ApiKeyInfo(
    @FieldDoc("The API key unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("Human-readable label describing where the key is used.")
    val label: String,
    @FieldDoc("Unix timestamp (ms) when the key was created.")
    val createdAt: Long,
    @FieldDoc("Unix timestamp (ms) of the last successful authentication with this key, if any.")
    val lastUsed: Long? = null,
    @FieldDoc("Unix timestamp (ms) at which the key expires, if ever.")
    val expiresAt: Long? = null,
    @FieldDoc("Whether the key has been revoked.")
    val isRevoked: Boolean = false,
)
