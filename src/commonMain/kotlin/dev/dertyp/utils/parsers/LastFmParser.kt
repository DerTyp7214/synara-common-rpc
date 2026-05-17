package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class LastFmParser : UrlParser() {
    override val name: String = "lastfm"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "last.fm" || host == "www.last.fm"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty() || pathParts[0] != "music") return null

        return when (pathParts.size) {
            2 -> pathParts[1] to Type.ARTIST
            3 -> "${pathParts[1]}/${pathParts[2]}" to Type.ALBUM
            4 -> "${pathParts[1]}/${pathParts[3]}" to Type.SONG
            else -> null
        }
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ARTIST -> "https://www.last.fm/music/$id"
            Type.ALBUM -> "https://www.last.fm/music/$id"
            Type.SONG -> {
                if (id.contains("/")) {
                    val artist = id.substringBefore("/")
                    val track = id.substringAfter("/")
                    "https://www.last.fm/music/$artist/_/$track"
                } else {
                    "https://www.last.fm/music/$id"
                }
            }
            else -> null
        }
    }
}
