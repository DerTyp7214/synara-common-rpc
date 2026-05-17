package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class OtotoyParser : UrlParser() {
    override val name: String = "ototoy"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "ototoy.jp"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        val pIndex = pathParts.indexOf("p")
        if (pIndex != -1 && pIndex + 1 < pathParts.size) {
            val id = pathParts[pIndex + 1]
            if (id.all { it.isDigit() }) return id to Type.ALBUM
        }

        val tIndex = pathParts.indexOf("t")
        if (tIndex != -1 && tIndex + 1 < pathParts.size) {
            val id = pathParts[tIndex + 1]
            if (id.all { it.isDigit() }) return id to Type.SONG
        }

        val aIndex = pathParts.indexOf("a")
        if (aIndex != -1 && aIndex + 1 < pathParts.size) {
            val id = pathParts[aIndex + 1]
            if (id.all { it.isDigit() }) return id to Type.ARTIST
        }

        return null
    }
}
