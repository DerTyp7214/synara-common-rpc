package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class JaxstaParser : UrlParser() {
    override val name: String = "jaxsta"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "jaxsta.com" || host == "www.jaxsta.com"
    }

    override suspend fun parse(url: String): Pair<String, Type>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to Type.ALBUM }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size >= 2 && pathParts[0] == "release") {
            val id = pathParts[1]
            if (id.contains("-") && id.length >= 32) return id to Type.ALBUM
        }

        return null
    }
}
