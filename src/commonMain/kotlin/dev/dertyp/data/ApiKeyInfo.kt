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
    @FieldDoc("Identifiers of the scopes granted to this key.")
    val scopes: List<String> = emptyList(),
)

@Serializable
@ModelDoc("Describes an API key scope that can be granted to keys.")
data class ApiKeyScopeInfo(
    @FieldDoc("The unique scope identifier.")
    val id: String,
    @FieldDoc("Human-readable scope name.")
    val name: String,
    @FieldDoc("Description of what the scope grants access to.")
    val description: String,
    @FieldDoc("Origin of the scope: \"server\" for built-in scopes, otherwise the id of the plugin that registered it.")
    val source: String,
)
