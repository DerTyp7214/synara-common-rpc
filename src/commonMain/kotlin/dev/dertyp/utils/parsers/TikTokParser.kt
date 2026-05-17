package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class TikTokParser : UrlParser() {
    override val name: String = "tiktok"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "music.tiktok.com" || host == "tiktok.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size >= 2) {
            val type = when (pathParts[0]) {
                "album" -> Type.ALBUM
                "track" -> Type.SONG
                "artist" -> Type.ARTIST
                else -> null
            }
            if (type != null) return pathParts[1] to type
        }

        return null
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ALBUM -> "https://music.tiktok.com/album/$id"
            Type.SONG -> "https://music.tiktok.com/track/$id"
            Type.ARTIST -> "https://music.tiktok.com/artist/$id"
            else -> null
        }
    }
}
