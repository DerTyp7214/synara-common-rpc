package dev.dertyp.rpc.rest.client

import dev.dertyp.serializers.AppJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

class RestRequest(val method: String, val isPublic: Boolean, vararg path: String) {

    val path: List<String> = path.toList()

    private val queryValues = mutableListOf<Pair<String, String>>()
    private val fields = mutableMapOf<String, JsonElement>()
    private var bareBody: String? = null

    val query: List<Pair<String, String>> get() = queryValues

    val body: String? get() = bareBody ?: fields.takeIf { it.isNotEmpty() }?.let { JsonObject(it).toString() }

    fun query(name: String, value: String?): RestRequest {
        if (value != null) queryValues += name to value
        return this
    }

    fun queryAll(name: String, values: Iterable<String>?): RestRequest {
        values?.forEach { queryValues += name to it }
        return this
    }

    fun <T> queryJson(name: String, serializer: KSerializer<T>, value: T?): RestRequest {
        if (value != null) queryValues += name to AppJson.encodeToString(serializer, value)
        return this
    }

    inline fun <reified T> queryJson(name: String, value: T?): RestRequest =
        queryJson(name, AppJson.serializersModule.serializer<T>(), value)

    fun <T> body(serializer: KSerializer<T>, value: T?): RestRequest {
        if (value != null) bareBody = AppJson.encodeToString(serializer, value)
        return this
    }

    inline fun <reified T> body(value: T?): RestRequest = body(AppJson.serializersModule.serializer<T>(), value)

    fun <T> field(name: String, serializer: KSerializer<T>, value: T?): RestRequest {
        if (value != null) fields[name] = AppJson.encodeToJsonElement(serializer, value)
        return this
    }

    inline fun <reified T> field(name: String, value: T?): RestRequest =
        field(name, AppJson.serializersModule.serializer<T>(), value)
}
