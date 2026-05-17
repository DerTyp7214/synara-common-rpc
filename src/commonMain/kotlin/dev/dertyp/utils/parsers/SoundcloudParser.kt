package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class SoundcloudParser : UrlParser() {
    override val name: String = "soundcloud"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "soundcloud.com" || host.endsWith(".soundcloud.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val host = uri.host.lowercase()
        if (host != "soundcloud.com" && !host.endsWith(".soundcloud.com")) return null

        val path = uri.encodedPath.trim('/')
        if (path.isEmpty()) return null

        val pathParts = path.split('/')
        return when (pathParts.size) {
            1 -> pathParts[0] to Type.ARTIST
            2 -> {
                if (pathParts[1] == "reposts") pathParts[0] to Type.ARTIST
                else path to Type.SONG
            }
            3 -> {
                if (pathParts[1] == "sets") path to Type.PLAYLIST
                else path to Type.SONG
            }
            else -> path to Type.SONG
        }
    }
}
