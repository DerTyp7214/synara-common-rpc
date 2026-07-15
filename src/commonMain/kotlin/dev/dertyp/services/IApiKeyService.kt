package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ApiKeyInfo
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Manages long-lived API keys for authenticating non-interactive clients (e.g. media players).")
interface IApiKeyService {
    @RpcDoc("Create a new API key and return the raw secret. The secret is shown only once and cannot be retrieved again.")
    suspend fun createApiKey(
        @RpcParamDoc("Human-readable label describing where the key will be used.") label: String
    ): String

    @RpcDoc("List metadata for all API keys belonging to the current user. Never returns the secrets.")
    suspend fun listApiKeys(): List<ApiKeyInfo>

    @RpcDoc("Revoke an API key by its identifier. Returns true if a key was revoked.")
    suspend fun revokeApiKey(
        @RpcParamDoc("The API key unique identifier.") id: PlatformUUID
    ): Boolean
}
