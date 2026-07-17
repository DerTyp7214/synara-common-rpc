package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ApiKeyInfo
import dev.dertyp.data.ApiKeyScopeInfo
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manages long-lived API keys for authenticating non-interactive clients (e.g. media players).")
interface IApiKeyService {
    @RpcDoc("Create a new API key and return the raw secret.")
    suspend fun createApiKey(
        @RpcParamDoc("Human-readable label describing where the key will be used.") label: String,
        @RpcParamDoc("Identifiers of the scopes to grant to the key. Must be registered scopes.") scopes: List<String>,
    ): String

    @RpcDoc("Get the raw secret of one of the current user's API keys. Returns null for keys created before secrets were stored.")
    suspend fun getApiKeyString(
        @RpcParamDoc("The API key unique identifier.") id: PlatformUUID
    ): String?

    @RpcDoc("List all scopes that can be granted to API keys.")
    suspend fun listAvailableScopes(): List<ApiKeyScopeInfo>

    @RpcDoc("List metadata for all API keys belonging to the current user. Never returns the secrets.")
    suspend fun listApiKeys(): List<ApiKeyInfo>

    @RpcDoc("Revoke an API key by its identifier. Returns true if a key was revoked.")
    suspend fun revokeApiKey(
        @RpcParamDoc("The API key unique identifier.") id: PlatformUUID
    ): Boolean
}
