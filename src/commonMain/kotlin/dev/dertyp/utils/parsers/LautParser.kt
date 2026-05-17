package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class LautParser : UrlParser() {
    override val name: String = "laut"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "laut.de" || host == "www.laut.de"
    }

    override suspend fun parse(url: String): Pair<String, Type>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to Type.ALBUM }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size >= 3 && pathParts[1] == "Alben") {
            val lastSegment = pathParts[2]
            val id = lastSegment.substringAfterLast("-")
            if (id.all { it.isDigit() }) return id to Type.ALBUM
        }

        if (pathParts.size == 1 && pathParts[0].isNotEmpty()) {
            return pathParts[0] to Type.ARTIST
        }

        return null
    }
}
