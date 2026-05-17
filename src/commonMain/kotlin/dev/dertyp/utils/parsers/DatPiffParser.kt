package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class DatPiffParser : UrlParser() {
    override val name: String = "datpiff"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "datpiff.com" || host == "www.datpiff.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty()) return null

        val lastSegment = pathParts.last()
        if (lastSegment.contains("-mixtape.") && lastSegment.endsWith(".html")) {
            val id = lastSegment.substringAfter("-mixtape.").substringBefore(".html")
            if (id.all { it.isDigit() }) return id to Type.ALBUM
        }

        return null
    }
}
