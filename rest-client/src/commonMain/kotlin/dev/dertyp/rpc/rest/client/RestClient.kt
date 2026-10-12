package dev.dertyp.rpc.rest.client

import dev.dertyp.data.ApiVersion
import dev.dertyp.serializers.AppJson
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.content.TextContent
import io.ktor.http.encodeURLPathPart
import io.ktor.http.takeFrom
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

private const val DEFAULT_CHUNK_SIZE = 4096
private const val SSE_DATA_FIELD = "data"

private class Outcome<R>(val value: R)

private fun URLBuilder.locate(baseUrl: String, request: RestRequest) {
    takeFrom(baseUrl)
    appendEncodedPathSegments(request.path.map { it.encodeURLPathPart() })
    request.query.forEach { (name, value) -> parameters.append(name, value) }
}

class RestClient(private val config: RestClientConfig) {

    suspend fun <T> json(serializer: KSerializer<T>, request: RestRequest): T =
        call(request) { AppJson.decodeFromString(serializer, it.bodyAsText()) }

    suspend inline fun <reified T> json(request: RestRequest): T =
        json(AppJson.serializersModule.serializer<T>(), request)

    suspend fun <T : Any> jsonOrNull(serializer: KSerializer<T>, request: RestRequest): T? =
        call<T?>(request, onNotFound = { null }) { AppJson.decodeFromString(serializer, it.bodyAsText()) }

    suspend inline fun <reified T : Any> jsonOrNull(request: RestRequest): T? =
        jsonOrNull(AppJson.serializersModule.serializer<T>(), request)

    suspend fun unit(request: RestRequest) {
        call(request) { }
    }

    suspend fun bytes(request: RestRequest): ByteArray = call(request) { it.bodyAsBytes() }

    suspend fun bytesOrNull(request: RestRequest): ByteArray? =
        call<ByteArray?>(request, onNotFound = { null }) { it.bodyAsBytes() }

    fun <T> sse(serializer: KSerializer<T>, request: RestRequest): Flow<T> = channelFlow {
        call(request, configure = { accept(ContentType.Text.EventStream) }) { response ->
            val channel = response.bodyAsChannel()
            val data = StringBuilder()
            var hasData = false
            while (true) {
                val line = channel.readLine() ?: break
                if (line.isEmpty()) {
                    if (hasData) send(AppJson.decodeFromString(serializer, data.toString()))
                    data.clear()
                    hasData = false
                    continue
                }
                if (line.startsWith(":")) continue
                val colon = line.indexOf(':')
                val field = if (colon < 0) line else line.substring(0, colon)
                if (field != SSE_DATA_FIELD) continue
                if (hasData) data.append('\n')
                if (colon >= 0) data.append(line.substring(colon + 1).removePrefix(" "))
                hasData = true
            }
        }
    }

    inline fun <reified T> sse(request: RestRequest): Flow<T> =
        sse(AppJson.serializersModule.serializer<T>(), request)

    fun byteFlow(request: RestRequest): Flow<ByteArray> = channelFlow {
        call(request) { response ->
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(DEFAULT_CHUNK_SIZE)
            while (true) {
                val read = channel.readAvailable(buffer)
                if (read < 0) break
                if (read > 0) send(buffer.copyOf(read))
            }
        }
    }

    fun file(request: RestRequest, offset: Long?, chunkSize: Int?): Flow<ByteArray> = channelFlow {
        val size = (chunkSize ?: DEFAULT_CHUNK_SIZE).coerceAtLeast(1)
        val range = offset?.takeIf { it > 0 }?.let { "bytes=$it-" }
        call(request, configure = { range?.let { headers[HttpHeaders.Range] = it } }) { response ->
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(size)
            var filled = 0
            while (true) {
                val read = channel.readAvailable(buffer, filled, size - filled)
                if (read < 0) break
                filled += read
                if (filled == size) {
                    send(buffer.copyOf())
                    filled = 0
                }
            }
            if (filled > 0) send(buffer.copyOf(filled))
        }
    }

    suspend fun url(request: RestRequest): Url = URLBuilder().apply { locate(config.baseUrl(), request) }.build()

    private suspend fun <R> call(
        request: RestRequest,
        configure: HttpRequestBuilder.() -> Unit = {},
        onNotFound: (() -> R)? = null,
        block: suspend (HttpResponse) -> R,
    ): R {
        val outcome = attempt(request, false, !request.isPublic, configure, onNotFound, block)
            ?: attempt(request, true, false, configure, onNotFound, block)
            ?: throw RestException(401, "Unauthorized")
        return outcome.value
    }

    private suspend fun <R> attempt(
        request: RestRequest,
        forceRefresh: Boolean,
        repeatOnUnauthorized: Boolean,
        configure: HttpRequestBuilder.() -> Unit,
        onNotFound: (() -> R)?,
        block: suspend (HttpResponse) -> R,
    ): Outcome<R>? {
        val baseUrl = config.baseUrl()
        val token = if (request.isPublic) null else config.token(forceRefresh)
        val statement = config.httpClient.prepareRequest {
            method = HttpMethod.parse(request.method)
            expectSuccess = false
            url { locate(baseUrl, request) }
            config.headers(this)
            headers[ApiVersion.HEADER] = ApiVersion.CURRENT.toString()
            token?.let { headers[HttpHeaders.Authorization] = "Bearer $it" }
            request.body?.let { setBody(TextContent(it, ContentType.Application.Json)) }
            configure()
        }
        return statement.execute { response ->
            val status = response.status.value
            when {
                status in 200..299 -> Outcome(block(response))
                status == 404 && onNotFound != null -> Outcome(onNotFound())
                status == 401 && repeatOnUnauthorized -> null
                status == 400 -> throw IllegalArgumentException(response.bodyAsText())
                else -> throw RestException(status, response.bodyAsText().ifEmpty { response.status.description })
            }
        }
    }
}
