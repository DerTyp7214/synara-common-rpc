package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class AudiusParser : UrlParser() {
    override val name: String = "audius"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "audius.co" || host.endsWith(".audius.co")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty() || pathParts[0].isEmpty()) return null

        val lastSegment = pathParts.last()
        val type = when {
            pathParts.contains("tracks") -> Type.SONG
            pathParts.contains("playlists") -> Type.PLAYLIST
            pathParts.contains("albums") -> Type.ALBUM
            else -> null
        }

        if (type != null) {
            return lastSegment to type
        }

        if (pathParts.size >= 2) {
            val idPart = lastSegment.substringAfterLast("-")
            if (idPart.all { it.isDigit() }) return idPart to Type.SONG
        }

        return null
    }
}
