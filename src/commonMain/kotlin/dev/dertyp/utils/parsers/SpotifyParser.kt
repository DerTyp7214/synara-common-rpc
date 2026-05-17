package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class SpotifyParser : UrlParser() {
    override val name: String = "spotify"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "open.spotify.com" || host == "spotify.com" || host.endsWith(".spotify.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val host = uri.host.lowercase()

        if (host != "open.spotify.com" && host != "spotify.com") return null

        val parts = uri.encodedPath.trim('/').split("/")
        if (parts.size < 2) return null

        val type = when (parts[0]) {
            "track" -> Type.SONG
            "album" -> Type.ALBUM
            "playlist" -> Type.PLAYLIST
            "artist" -> Type.ARTIST
            "show" -> Type.PLAYLIST
            "episode" -> Type.SONG
            else -> return null
        }

        val id = parts[1]

        return id to type
    }
}
