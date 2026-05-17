package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class AllMusicParser : UrlParser() {
    override val name: String = "allmusic"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "allmusic.com" || host == "www.allmusic.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty() || pathParts[0].isEmpty()) return null

        val lastSegment = pathParts.last()
        val id = if (lastSegment.contains("-")) {
            lastSegment.substringAfterLast("-")
        } else {
            lastSegment
        }

        val type = when {
            id.startsWith("mw") -> Type.ALBUM
            id.startsWith("mn") -> Type.ARTIST
            id.startsWith("mt") -> Type.SONG
            else -> null
        }

        return if (type != null) id to type else null
    }
}
