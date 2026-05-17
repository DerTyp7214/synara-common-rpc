package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class HdTracksParser : UrlParser() {
    override val name: String = "hdtracks"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "hdtracks.com" || host == "www.hdtracks.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val path = if (uri.fragment.startsWith("/album/")) {
            uri.fragment.removePrefix("/album/")
        } else {
            uri.encodedPath.substringAfter("/album/", "")
        }

        if (path.isEmpty()) return null
        
        val id = path.trim('/').split("/")[0]
        if (id.length == 24 && id.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            return id to Type.ALBUM
        }

        return null
    }
}
