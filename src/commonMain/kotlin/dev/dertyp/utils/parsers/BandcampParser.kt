package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class BandcampParser : UrlParser() {
    override val name: String = "bandcamp"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host.contains(".bandcamp.com") || host == "bandcamp.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val host = uri.host.lowercase()
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (host == "bandcamp.com") {
            if (pathParts.contains("EmbeddedPlayer")) {
                val params = uri.parameters
                params["album"]?.let { return it to Type.ALBUM }
                params["track"]?.let { return it to Type.SONG }
            }
            return null
        }

        val subdomain = host.removeSuffix(".bandcamp.com")
        
        return when {
            pathParts.size >= 2 && pathParts[0] == "album" -> {
                "$subdomain/album/${pathParts[1]}" to Type.ALBUM
            }
            pathParts.size >= 2 && pathParts[0] == "track" -> {
                "$subdomain/track/${pathParts[1]}" to Type.SONG
            }
            pathParts.isEmpty() || pathParts[0].isEmpty() -> {
                subdomain to Type.ARTIST
            }
            else -> "$subdomain/${pathParts.joinToString("/")}" to Type.ALBUM
        }
    }

    override fun toUrl(id: String, type: Type): String? {
        val subdomain = id.substringBefore("/")
        val rest = id.substringAfter("/")
        return when (type) {
            Type.ALBUM -> "https://$subdomain.bandcamp.com/$rest"
            Type.SONG -> "https://$subdomain.bandcamp.com/$rest"
            Type.ARTIST -> "https://$subdomain.bandcamp.com"
            else -> null
        }
    }
}
