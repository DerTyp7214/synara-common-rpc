package dev.dertyp.rpc.rest.client

import io.ktor.client.HttpClient
import io.ktor.http.HttpMessageBuilder

class RestClientConfig(
    val httpClient: HttpClient,
    val baseUrl: suspend () -> String,
    val token: suspend (forceRefresh: Boolean) -> String?,
    val headers: HttpMessageBuilder.() -> Unit = {},
)
