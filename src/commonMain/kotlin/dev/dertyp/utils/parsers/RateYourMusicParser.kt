package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class RateYourMusicParser : UrlParser() {
    override val name: String = "rateyourmusic"
    override val alternativeNames: List<String> = listOf("rym")

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "rateyourmusic.com" || host == "sonemic.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")
        if (pathParts.isEmpty()) return null

        if (pathParts[0] == "release" && pathParts.size >= 4) {
            val rymType = pathParts[1]
            val type = when (rymType) {
                "album", "ep", "comp", "mixtape", "single", "djmix", "bootleg" -> Type.ALBUM
                "video" -> Type.VIDEO
                else -> return null
            }
            return pathParts.drop(1).joinToString("/") to type
        }
        
        if (pathParts[0] == "artist" && pathParts.size >= 2) {
            return pathParts[1] to Type.ARTIST
        }

        return null
    }

    override fun toUrl(id: String, type: Type): String? {
        return when (type) {
            Type.ALBUM, Type.VIDEO -> "https://rateyourmusic.com/release/$id"
            Type.ARTIST -> "https://rateyourmusic.com/artist/$id"
            else -> null
        }
    }
}
