package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class YoutubeParser : UrlParser() {
    override val name: String = "youtube"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "youtu.be" || host == "youtube.com" || host.endsWith(".youtube.com")
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val host = uri.host.lowercase()

        if (host == "youtu.be") {
            val id = uri.encodedPath.trim('/')
            if (id.isEmpty()) return null
            return id to Type.SONG
        }

        if (host == "youtube.com" || host.endsWith(".youtube.com")) {
            uri.parameters["v"]?.let { return it to Type.SONG }
            uri.parameters["list"]?.let { return it to Type.PLAYLIST }

            if (uri.encodedPath.startsWith("/shorts/")) {
                return uri.encodedPath.removePrefix("/shorts/").trim('/') to Type.SONG
            }

            if (uri.encodedPath.startsWith("/channel/") || uri.encodedPath.startsWith("/user/") || uri.encodedPath.startsWith("/@")) {
                return uri.encodedPath.trim('/') to Type.ARTIST
            }
        }

        return null
    }
}
