package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class SevenDigitalParser : UrlParser() {
    override val name: String = "7digital"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host.contains("7digital.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty()) return null

        val releaseIndex = pathParts.indexOf("release")
        if (releaseIndex != -1 && releaseIndex + 1 < pathParts.size) {
            val segment = pathParts[releaseIndex + 1]
            val id = segment.substringAfterLast("-")
            if (id.all { it.isDigit() }) return id to Type.ALBUM
        }

        return null
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ALBUM -> "https://us.7digital.com/artist/a/release/b-$id"
            else -> null
        }
    }
}
