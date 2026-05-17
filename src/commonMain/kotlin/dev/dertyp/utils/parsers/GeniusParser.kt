package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class GeniusParser : UrlParser() {
    override val name: String = "genius"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "genius.com" || host.endsWith(".genius.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty() || pathParts[0].isEmpty()) return null

        return when {
            pathParts.size >= 3 && pathParts[0] == "albums" -> {
                "${pathParts[1]}/${pathParts[2]}" to Type.ALBUM
            }
            pathParts.size >= 2 && pathParts[0] == "artists" -> {
                pathParts[1] to Type.ARTIST
            }
            pathParts[0].endsWith("-lyrics") -> {
                pathParts[0].removeSuffix("-lyrics") to Type.SONG
            }
            else -> pathParts[0] to Type.SONG
        }
    }
}
