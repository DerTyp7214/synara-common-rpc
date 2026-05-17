package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

class JunoDownloadParser : UrlParser() {
    override val name: String = "junodownload"

    override fun canHandle(url: String): Boolean {
        if (handlePrefix(url) != null) return true
        val host = getUri(url)?.host?.lowercase() ?: ""
        return host == "junodownload.com" || host == "www.junodownload.com"
    }

    override suspend fun parse(url: String): Pair<String, Type?>? {
        if (!canHandle(url)) return null
        handlePrefix(url)?.let { return it to null }

        val uri = getUri(url) ?: return null
        val pathParts = uri.encodedPath.trim('/').split("/")

        if (pathParts.size >= 2 && pathParts[0] == "products") {
            val idSegment = pathParts.last().removeSuffix(".htm").removeSuffix(".html")
            if (idSegment.contains("-") && idSegment.replace("-", "").all { it.isDigit() }) {
                return idSegment to Type.ALBUM
            }
            if (idSegment.all { it.isDigit() }) {
                return idSegment to Type.ALBUM
            }
        }

        return null
    }
}
