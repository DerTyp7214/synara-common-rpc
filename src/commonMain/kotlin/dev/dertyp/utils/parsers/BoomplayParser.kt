package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class BoomplayParser : UrlParser() {
    override val name: String = "boomplay"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "boomplay.com" || host.endsWith(".boomplay.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        val type = when {
            pathParts.contains("albums") || pathParts.contains("album") -> Type.ALBUM
            pathParts.contains("songs") || pathParts.contains("music") -> Type.SONG
            pathParts.contains("artists") -> Type.ARTIST
            pathParts.contains("playlists") -> Type.PLAYLIST
            else -> null
        }

        val id = pathParts.lastOrNull()
        if (id != null && id.all { it.isDigit() }) {
            return id to (type ?: return null)
        }

        return null
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ALBUM -> "https://www.boomplay.com/albums/$id"
            Type.SONG -> "https://www.boomplay.com/songs/$id"
            Type.ARTIST -> "https://www.boomplay.com/artists/$id"
            Type.PLAYLIST -> "https://www.boomplay.com/playlists/$id"
            else -> null
        }
    }
}
