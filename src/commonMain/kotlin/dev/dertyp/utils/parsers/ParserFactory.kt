package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type

object ParserFactory {
    private val parsers = listOf(
        YoutubeParser(),
        SoundcloudParser(),
        TidalParser(),
        SpotifyParser(),
        MusicBrainzParser(),
        ListenBrainzParser(),
        AmazonParser(),
        AppleMusicParser(),
        DeezerParser(),
        YandexParser(),
        PandoraParser(),
        BeatportParser(),
        BoomplayParser(),
        DiscogsParser(),
        RateYourMusicParser(),
        WikidataParser(),
        MoraParser(),
        NapsterParser(),
        QobuzParser(),
        AnghamiParser(),
        LiveMixtapesParser(),
        MusikSammlerParser(),
        BandcampParser(),
        GeniusParser(),
        AllMusicParser(),
        LastFmParser(),
        DatPiffParser(),
        AudiomackParser(),
        SpinrillaParser(),
        JaxstaParser(),
        JunoDownloadParser(),
        OtotoyParser(),
        HdTracksParser(),
        AudiusParser(),
        SevenDigitalParser(),
        OffizielleChartsParser(),
        LautParser(),
        TikTokParser()
    )

    fun getParser(url: String): UrlParser? {
        if (url.contains(":") && !url.contains("://")) {
            val provider = url.substringBefore(":")
            return getParserForProvider(provider)
        }
        return parsers.find { it.canHandle(url) }
    }

    fun getParserForProvider(provider: String): UrlParser? {
        return parsers.find {
            it.name.equals(provider, ignoreCase = true) ||
                    it.alternativeNames.any { alt -> alt.equals(provider, ignoreCase = true) }
        }
    }

    fun toUrl(provider: String, id: String, type: Type): String? {
        return getParserForProvider(provider)?.toUrl(id, type)
    }

    fun toUrl(prefixedId: String, type: Type): String? {
        val provider = prefixedId.substringBefore(":")
        val id = prefixedId.substringAfter(":")
        return toUrl(provider, id, type)
    }
}
