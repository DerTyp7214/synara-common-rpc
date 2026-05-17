package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class MusicBrainzParser : UrlParser() {
    override val name: String = "musicbrainz"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "musicbrainz.org" || host == "www.musicbrainz.org"
    }

    override suspend fun parse(url: String): Pair<String, Type>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to Type.ALBUM }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size < 2) return null

        val type = when (pathParts[0]) {
            "recording" -> Type.SONG
            "release", "release-group" -> Type.ALBUM
            "artist" -> Type.ARTIST
            "series" -> Type.PLAYLIST
            else -> return null
        }

        return pathParts[1] to type
    }
}
