package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class ListenBrainzParser : UrlParser() {
    override val name: String = "listenbrainz"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "listenbrainz.org" || host == "www.listenbrainz.org"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size < 2) return null

        val type = when (pathParts[0]) {
            "artist" -> Type.ARTIST
            "release-group" -> Type.ALBUM
            "playlist" -> Type.PLAYLIST
            else -> return null
        }

        return pathParts[1] to type
    }
}
