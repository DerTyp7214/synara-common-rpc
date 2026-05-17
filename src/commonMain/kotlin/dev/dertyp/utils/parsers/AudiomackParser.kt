package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class AudiomackParser : UrlParser() {
    override val name: String = "audiomack"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "audiomack.com" || host.endsWith(".audiomack.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty() || pathParts[0].isEmpty()) return null

        return when {
            pathParts.size >= 3 && pathParts[0] == "album" -> {
                "${pathParts[1]}/${pathParts[2]}" to Type.ALBUM
            }
            pathParts.size >= 3 && pathParts[1] == "album" -> {
                "${pathParts[0]}/${pathParts[2]}" to Type.ALBUM
            }
            pathParts.size >= 3 && pathParts[0] == "song" -> {
                "${pathParts[1]}/${pathParts[2]}" to Type.SONG
            }
            pathParts.size >= 3 && pathParts[1] == "song" -> {
                "${pathParts[0]}/${pathParts[2]}" to Type.SONG
            }
            pathParts.size >= 3 && pathParts[0] == "playlist" -> {
                "${pathParts[1]}/${pathParts[2]}" to Type.PLAYLIST
            }
            pathParts.size >= 3 && pathParts[1] == "playlist" -> {
                "${pathParts[0]}/${pathParts[2]}" to Type.PLAYLIST
            }
            pathParts.size == 1 -> {
                pathParts[0] to Type.ARTIST
            }
            else -> null
        }
    }
}
