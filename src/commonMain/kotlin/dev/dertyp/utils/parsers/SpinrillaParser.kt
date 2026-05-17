package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class SpinrillaParser : UrlParser() {
    override val name: String = "spinrilla"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "spinrilla.com" || host.endsWith(".spinrilla.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size >= 2 && pathParts[0] == "mixtapes") {
            return pathParts[1] to Type.ALBUM
        }

        return null
    }
}
