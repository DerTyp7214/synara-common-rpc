package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class OffizielleChartsParser : UrlParser() {
    override val name: String = "offiziellecharts"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "offiziellecharts.de" || host == "www.offiziellecharts.de"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty()) return null

        val lastSegment = pathParts.last()
        return when {
            lastSegment.startsWith("album-details-") -> {
                lastSegment.removePrefix("album-details-") to Type.ALBUM
            }
            lastSegment.startsWith("titel-details-") -> {
                lastSegment.removePrefix("titel-details-") to Type.SONG
            }
            else -> null
        }
    }
}
