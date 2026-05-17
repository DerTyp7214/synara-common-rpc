package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class YandexParser : UrlParser() {
    override val name: String = "yandex"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "music.yandex.ru" || host == "music.yandex.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        val trackIndex = pathParts.indexOf("track")
        if (trackIndex != -1 && trackIndex + 1 < pathParts.size) {
            return pathParts[trackIndex + 1] to Type.SONG
        }

        val albumIndex = pathParts.indexOf("album")
        if (albumIndex != -1 && albumIndex + 1 < pathParts.size) {
            return pathParts[albumIndex + 1] to Type.ALBUM
        }

        val artistIndex = pathParts.indexOf("artist")
        if (artistIndex != -1 && artistIndex + 1 < pathParts.size) {
            return pathParts[artistIndex + 1] to Type.ARTIST
        }

        return null
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ALBUM -> "https://music.yandex.ru/album/$id"
            Type.SONG -> "https://music.yandex.ru/track/$id"
            Type.ARTIST -> "https://music.yandex.ru/artist/$id"
            else -> null
        }
    }
}
