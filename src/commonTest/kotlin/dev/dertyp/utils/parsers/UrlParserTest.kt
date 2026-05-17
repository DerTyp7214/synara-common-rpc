package dev.dertyp.utils.parsers

import dev.dertyp.services.import.Type
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@Suppress("HttpUrlsUsage")
class UrlParserTest {

    enum class ParserTestData(
        val parser: UrlParser,
        val testCases: List<Pair<String, Pair<String, Type?>?>>,
        val toUrlCases: List<Pair<Pair<String, Type>, String?>> = emptyList()
    ) {
        YOUTUBE(
            YoutubeParser(), listOf(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to ("dQw4w9WgXcQ" to Type.SONG),
                "https://youtu.be/dQw4w9WgXcQ" to ("dQw4w9WgXcQ" to Type.SONG),
                "https://www.youtube.com/shorts/dQw4w9WgXcQ" to ("dQw4w9WgXcQ" to Type.SONG),
                "https://music.youtube.com/watch?v=dQw4w9WgXcQ" to ("dQw4w9WgXcQ" to Type.SONG),
                "https://www.youtube.com/playlist?list=PL12345" to ("PL12345" to Type.PLAYLIST),
                "https://www.youtube.com/channel/UC123" to ("channel/UC123" to Type.ARTIST),
                "https://www.youtube.com/user/testuser" to ("user/testuser" to Type.ARTIST),
                "https://www.youtube.com/@testartist" to ("@testartist" to Type.ARTIST),
                "https://example.com/watch?v=dQw4w9WgXcQ" to null,
                "not a url" to null
            ),
            listOf(
                ("dQw4w9WgXcQ" to Type.SONG) to "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                ("PL12345" to Type.PLAYLIST) to "https://www.youtube.com/playlist?list=PL12345",
                ("@testartist" to Type.ARTIST) to "https://www.youtube.com/@testartist",
                ("123" to Type.ALBUM) to null
            )
        ),
        SOUNDCLOUD(
            SoundcloudParser(), listOf(
                "https://soundcloud.com/artist/song" to ("artist/song" to Type.SONG),
                "https://soundcloud.com/artist/song/more" to ("artist/song/more" to Type.SONG),
                "https://soundcloud.com/artist/sets/playlist" to ("artist/sets/playlist" to Type.PLAYLIST),
                "https://soundcloud.com/artist" to ("artist" to Type.ARTIST),
                "https://soundcloud.com/artist/reposts" to ("artist" to Type.ARTIST),
                "https://example.com/artist/song" to null
            ),
            listOf(
                ("artist/song" to Type.SONG) to "https://soundcloud.com/artist/song",
                ("artist" to Type.ARTIST) to "https://soundcloud.com/artist",
                ("artist/sets/playlist" to Type.PLAYLIST) to "https://soundcloud.com/artist/sets/playlist"
            )
        ),
        TIDAL(
            TidalParser(), listOf(
                "https://tidal.com/track/12345" to ("12345" to Type.SONG),
                "https://tidal.com/browse/track/12345" to ("12345" to Type.SONG),
                "https://tidal.com/album/67890" to ("67890" to Type.ALBUM),
                "https://listen.tidal.com/album/11343637" to ("11343637" to Type.ALBUM),
                "https://tidal.com/playlist/abcde" to ("abcde" to Type.PLAYLIST),
                "https://tidal.com/artist/fghij" to ("fghij" to Type.ARTIST),
                "https://tidal.com/video/klmno" to ("klmno" to Type.VIDEO),
                "tiddl:12345" to ("12345" to null),
                "tdn:12345" to ("12345" to null),
                "tidal:12345" to ("12345" to null)
            ),
            listOf(
                ("12345" to Type.SONG) to "https://tidal.com/track/12345",
                ("67890" to Type.ALBUM) to "https://tidal.com/album/67890",
                ("fghij" to Type.ARTIST) to "https://tidal.com/artist/fghij"
            )
        ),
        SPOTIFY(
            SpotifyParser(), listOf(
                "https://open.spotify.com/track/123" to ("123" to Type.SONG),
                "https://open.spotify.com/episode/123" to ("123" to Type.SONG),
                "https://open.spotify.com/album/456" to ("456" to Type.ALBUM),
                "https://open.spotify.com/playlist/789" to ("789" to Type.PLAYLIST),
                "https://open.spotify.com/show/789" to ("789" to Type.PLAYLIST),
                "https://open.spotify.com/artist/abc" to ("abc" to Type.ARTIST),
                "spotify:123" to ("123" to null)
            ),
            listOf(
                ("123" to Type.SONG) to "https://open.spotify.com/track/123",
                ("456" to Type.ALBUM) to "https://open.spotify.com/album/456",
                ("abc" to Type.ARTIST) to "https://open.spotify.com/artist/abc"
            )
        ),
        MUSICBRAINZ(
            MusicBrainzParser(), listOf(
                "https://musicbrainz.org/recording/rec-id" to ("rec-id" to Type.SONG),
                "https://musicbrainz.org/release/rel-id" to ("rel-id" to Type.ALBUM),
                "https://musicbrainz.org/release-group/rg-id" to ("rg-id" to Type.ALBUM),
                "https://musicbrainz.org/artist/art-id" to ("art-id" to Type.ARTIST),
                "https://musicbrainz.org/series/ser-id" to ("ser-id" to Type.PLAYLIST)
            ),
            listOf(
                ("rec-id" to Type.SONG) to "https://musicbrainz.org/recording/rec-id",
                ("rel-id" to Type.ALBUM) to "https://musicbrainz.org/release-group/rel-id",
                ("art-id" to Type.ARTIST) to "https://musicbrainz.org/artist/art-id"
            )
        ),
        LISTENBRAINZ(
            ListenBrainzParser(), listOf(
                "https://listenbrainz.org/artist/art-id" to ("art-id" to Type.ARTIST),
                "https://listenbrainz.org/release-group/rg-id" to ("rg-id" to Type.ALBUM),
                "https://listenbrainz.org/playlist/pl-id" to ("pl-id" to Type.PLAYLIST)
            ),
            listOf(
                ("art-id" to Type.ARTIST) to "https://listenbrainz.org/artist/art-id",
                ("rg-id" to Type.ALBUM) to "https://listenbrainz.org/release-group/rg-id"
            )
        ),
        AMAZON(
            AmazonParser(), listOf(
                "https://www.amazon.de/gp/product/B01M4OCFDH" to ("B01M4OCFDH" to Type.ALBUM),
                "https://amazon.com/dp/B07HRCQCNB" to ("B07HRCQCNB" to Type.ALBUM),
                "https://music.amazon.com/albums/B009YARQU4" to ("B009YARQU4" to Type.ALBUM),
                "https://music.amazon.com/albums/B0064UPU4G?trackAsin=B0064UPUDC" to ("B0064UPUDC" to Type.SONG),
                "https://music.amazon.com/tracks/B076YT2CBT" to ("B076YT2CBT" to Type.SONG)
            ),
            listOf(
                ("B01M4OCFDH" to Type.ALBUM) to "https://music.amazon.com/albums/B01M4OCFDH",
                ("B076YT2CBT" to Type.SONG) to "https://music.amazon.com/tracks/B076YT2CBT"
            )
        ),
        APPLE(
            AppleMusicParser(), listOf(
                "https://music.apple.com/us/album/evermore/1544268285" to ("1544268285" to Type.ALBUM),
                "https://music.apple.com/us/album/evermore/1544268285?i=1544268286" to ("1544268286" to Type.SONG),
                "https://music.apple.com/us/music-video/willow/1544520973" to ("1544520973" to Type.VIDEO),
                "https://itunes.apple.com/de/album/id571919008" to ("571919008" to Type.ALBUM),
                "https://geo.music.apple.com/us/album/_/571919008?mt=1" to ("571919008" to Type.ALBUM)
            ),
            listOf(
                ("1544268285" to Type.ALBUM) to "https://music.apple.com/album/1544268285",
                ("1544268286" to Type.SONG) to "https://music.apple.com/song/1544268286",
                ("abc" to Type.ARTIST) to "https://music.apple.com/artist/abc"
            )
        ),
        DEEZER(
            DeezerParser(), listOf(
                "https://www.deezer.com/album/610328042" to ("610328042" to Type.ALBUM),
                "https://deezer.com/track/3135556" to ("3135556" to Type.SONG)
            ),
            listOf(
                ("610328042" to Type.ALBUM) to "https://www.deezer.com/album/610328042",
                ("3135556" to Type.SONG) to "https://www.deezer.com/track/3135556"
            )
        ),
        YANDEX(
            YandexParser(), listOf(
                "https://music.yandex.ru/album/3882209" to ("3882209" to Type.ALBUM),
                "https://music.yandex.ru/album/9881481/track/62579582" to ("62579582" to Type.SONG)
            ),
            listOf(
                ("3882209" to Type.ALBUM) to "https://music.yandex.ru/album/3882209",
                ("62579582" to Type.SONG) to "https://music.yandex.ru/track/62579582"
            )
        ),
        PANDORA(
            PandoraParser(), listOf(
                "https://www.pandora.com/AL:11435696" to ("AL:11435696" to Type.ALBUM),
                "https://pandora.com/track/name/TR:11423273" to ("TR:11423273" to Type.SONG)
            ),
            listOf(
                ("AL:11435696" to Type.ALBUM) to "https://www.pandora.com/AL:11435696",
                ("TR:11423273" to Type.SONG) to "https://www.pandora.com/TR:11423273"
            )
        ),
        BEATPORT(
            BeatportParser(), listOf(
                "https://www.beatport.com/release/slug/1702043" to ("1702043" to Type.ALBUM),
                "https://www.beatport.com/track/slug/23011269" to ("23011269" to Type.SONG),
                "https://www.beatport.com/artist/slug/123" to ("123" to Type.ARTIST)
            ),
            listOf(
                ("1702043" to Type.ALBUM) to "https://www.beatport.com/release/slug/1702043",
                ("23011269" to Type.SONG) to "https://www.beatport.com/track/slug/23011269",
                ("123" to Type.ARTIST) to "https://www.beatport.com/artist/slug/123"
            )
        ),
        BOOMPLAY(
            BoomplayParser(), listOf(
                "https://www.boomplay.com/albums/8411102" to ("8411102" to Type.ALBUM),
                "https://www.boomplay.com/songs/74767514" to ("74767514" to Type.SONG),
                "https://www.boomplay.com/share/album/40002743" to ("40002743" to Type.ALBUM)
            ),
            listOf(
                ("8411102" to Type.ALBUM) to "https://www.boomplay.com/albums/8411102",
                ("74767514" to Type.SONG) to "https://www.boomplay.com/songs/74767514",
                ("123" to Type.ARTIST) to "https://www.boomplay.com/artists/123"
            )
        ),
        DISCOGS(
            DiscogsParser(), listOf(
                "https://www.discogs.com/release/7049051" to ("7049051" to Type.ALBUM),
                "https://www.discogs.com/master/26647-slug" to ("26647" to Type.ALBUM)
            ),
            listOf(
                ("7049051" to Type.ALBUM) to "https://www.discogs.com/release/7049051"
            )
        ),
        RATEYOURMUSIC(
            RateYourMusicParser(), listOf(
                "https://rateyourmusic.com/release/album/achtvier-bonez-mc/zwei-assis-trumpfen-aus/" to ("album/achtvier-bonez-mc/zwei-assis-trumpfen-aus" to Type.ALBUM),
                "https://rateyourmusic.com/artist/achtvier" to ("achtvier" to Type.ARTIST)
            ),
            listOf(
                ("album/a/b" to Type.ALBUM) to "https://rateyourmusic.com/release/album/a/b",
                ("achtvier" to Type.ARTIST) to "https://rateyourmusic.com/artist/achtvier"
            )
        ),
        WIKIDATA(
            WikidataParser(), listOf(
                "https://www.wikidata.org/wiki/Q127446878" to ("Q127446878" to Type.ALBUM)
            ),
            listOf(
                ("Q127446878" to Type.ALBUM) to "https://www.wikidata.org/wiki/Q127446878"
            )
        ),
        MORA(
            MoraParser(), listOf(
                "https://mora.jp/package/43000006/00602465618013/" to ("00602465618013" to Type.ALBUM),
                "https://mora.jp/track/43000006/00602465617924/1/" to ("00602465617924" to Type.SONG)
            ),
            listOf(
                ("00602465618013" to Type.ALBUM) to "https://mora.jp/package/00602465618013",
                ("00602465617924" to Type.SONG) to "https://mora.jp/track/00602465617924"
            )
        ),
        NAPSTER(
            NapsterParser(), listOf(
                "https://play.napster.com/album/alb.595142205" to ("alb.595142205" to Type.ALBUM),
                "https://web.napster.com/track/tra.123" to ("tra.123" to Type.SONG)
            ),
            listOf(
                ("alb.595142205" to Type.ALBUM) to "https://play.napster.com/alb.595142205",
                ("tra.123" to Type.SONG) to "https://play.napster.com/tra.123"
            )
        ),
        QOBUZ(
            QobuzParser(), listOf(
                "https://www.qobuz.com/de-de/album/vulcano-ep-bonez-mc-raf-camora/yxz0pt2qy7jhb" to ("yxz0pt2qy7jhb" to Type.ALBUM),
                "https://open.qobuz.com/track/12345" to ("12345" to Type.SONG)
            ),
            listOf(
                ("yxz0pt2qy7jhb" to Type.ALBUM) to "https://www.qobuz.com/album/yxz0pt2qy7jhb",
                ("12345" to Type.SONG) to "https://www.qobuz.com/track/12345"
            )
        ),
        ANGHAMI(
            AnghamiParser(), listOf(
                "https://play.anghami.com/album/4129825" to ("4129825" to Type.ALBUM),
                "https://play.anghami.com/song/1267509588" to ("1267509588" to Type.SONG)
            ),
            listOf(
                ("4129825" to Type.ALBUM) to "https://play.anghami.com/album/4129825",
                ("1267509588" to Type.SONG) to "https://play.anghami.com/song/1267509588",
                ("123" to Type.ARTIST) to "https://play.anghami.com/artist/123"
            )
        ),
        LIVEMIXTAPES(
            LiveMixtapesParser(), listOf(
                "https://www.livemixtapes.com/mixtapes/15113/slug.html" to ("15113" to Type.ALBUM),
                "https://www.livemixtapes.com/download/123/slug.html" to ("123" to Type.ALBUM)
            ),
            listOf(
                ("15113" to Type.ALBUM) to "https://www.livemixtapes.com/mixtapes/15113"
            )
        ),
        MUSIKSAMMLER(
            MusikSammlerParser(), listOf(
                "https://www.musik-sammler.de/album/568467/" to ("568467" to Type.ALBUM)
            ),
            listOf(
                ("568467" to Type.ALBUM) to "https://www.musik-sammler.de/album/568467/"
            )
        ),
        BANDCAMP(
            BandcampParser(), listOf(
                "https://thekali.bandcamp.com/album/nirvana" to ("thekali/album/nirvana" to Type.ALBUM),
                "https://cartierchain.bandcamp.com/track/song" to ("cartierchain/track/song" to Type.SONG),
                "https://teminite.bandcamp.com" to ("teminite" to Type.ARTIST)
            ),
            listOf(
                ("thekali/album/nirvana" to Type.ALBUM) to "https://thekali.bandcamp.com/album/nirvana",
                ("cartierchain/track/song" to Type.SONG) to "https://cartierchain.bandcamp.com/track/song",
                ("teminite" to Type.ARTIST) to "https://teminite.bandcamp.com"
            )
        ),
        GENIUS(
            GeniusParser(), listOf(
                "https://genius.com/albums/Aaron-cartier-and-blvth/Sorrynotsorry" to ("Aaron-cartier-and-blvth/Sorrynotsorry" to Type.ALBUM),
                "https://genius.com/Madonna-and-quavo-future-lyrics" to ("Madonna-and-quavo-future" to Type.SONG)
            ),
            listOf(
                ("Aaron-cartier-and-blvth/Sorrynotsorry" to Type.ALBUM) to "https://genius.com/albums/Aaron-cartier-and-blvth/Sorrynotsorry",
                ("Madonna-and-quavo-future" to Type.SONG) to "https://genius.com/Madonna-and-quavo-future-lyrics"
            )
        ),
        ALLMUSIC(
            AllMusicParser(), listOf(
                "https://www.allmusic.com/album/mw0003563247" to ("mw0003563247" to Type.ALBUM),
                "https://www.allmusic.com/album/thriller-mw0000053046" to ("mw0000053046" to Type.ALBUM)
            ),
            listOf(
                ("mw0003563247" to Type.ALBUM) to "https://www.allmusic.com/album/mw0003563247",
                ("mt0013834783" to Type.SONG) to "https://www.allmusic.com/song/mt0013834783"
            )
        ),
        LASTFM(
            LastFmParser(), listOf(
                "https://www.last.fm/music/Ufo361/Bald+ist+dein+Geld+meins" to ("Ufo361/Bald+ist+dein+Geld+meins" to Type.ALBUM),
                "https://www.last.fm/music/21+Savage" to ("21+Savage" to Type.ARTIST)
            ),
            listOf(
                ("21+Savage" to Type.ARTIST) to "https://www.last.fm/music/21+Savage",
                ("Ufo361/Bald+ist+dein+Geld+meins" to Type.ALBUM) to "https://www.last.fm/music/Ufo361/Bald+ist+dein+Geld+meins",
                ("Artist/Track" to Type.SONG) to "https://www.last.fm/music/Artist/_/Track"
            )
        ),
        DATPIFF(
            DatPiffParser(), listOf(
                "http://www.datpiff.com/Eminem-Eminem-Unreleased-And-Rare-mixtape.214859.html" to ("214859" to Type.ALBUM)
            ),
            listOf(
                ("214859" to Type.ALBUM) to "https://www.datpiff.com/mixtape.214859.html"
            )
        ),
        AUDIOMACK(
            AudiomackParser(), listOf(
                "https://www.audiomack.com/album/dj-day-day/56-nights-no-dj" to ("dj-day-day/56-nights-no-dj" to Type.ALBUM),
                "https://audiomack.com/eminem/song/killshot" to ("eminem/killshot" to Type.SONG)
            ),
            listOf(
                ("dj-day-day/56-nights-no-dj" to Type.ALBUM) to "https://audiomack.com/dj-day-day/album/56-nights-no-dj",
                ("eminem/killshot" to Type.SONG) to "https://audiomack.com/eminem/song/killshot"
            )
        ),
        SPINRILLA(
            SpinrillaParser(), listOf(
                "https://spinrilla.com/mixtapes/future-super-slimey" to ("future-super-slimey" to Type.ALBUM)
            ),
            listOf(
                ("future-super-slimey" to Type.ALBUM) to "https://spinrilla.com/mixtapes/future-super-slimey"
            )
        ),
        JAXSTA(
            JaxstaParser(), listOf(
                "https://jaxsta.com/release/c1c887d4-b2ee-5841-8f6e-d9f4a5d54051/428dc68a-bd77-58bf-afaa-4951fed1d08c" to ("c1c887d4-b2ee-5841-8f6e-d9f4a5d54051" to Type.ALBUM)
            ),
            listOf(
                ("c1c887d4-b2ee-5841-8f6e-d9f4a5d54051" to Type.ALBUM) to "https://jaxsta.com/release/c1c887d4-b2ee-5841-8f6e-d9f4a5d54051"
            )
        ),
        JUNODOWNLOAD(
            JunoDownloadParser(), listOf(
                "https://www.junodownload.com/products/2769522-02/" to ("2769522-02" to Type.ALBUM)
            ),
            listOf(
                ("2769522-02" to Type.ALBUM) to "https://www.junodownload.com/products/2769522-02/"
            )
        ),
        OTOTOY(
            OtotoyParser(), listOf(
                "https://ototoy.jp/_/default/p/1034847" to ("1034847" to Type.ALBUM)
            ),
            listOf(
                ("1034847" to Type.ALBUM) to "https://ototoy.jp/_/default/p/1034847"
            )
        ),
        HDTRACKS(
            HdTracksParser(), listOf(
                "https://www.hdtracks.com/#/album/5dfeb08e14babe527f83a26a" to ("5dfeb08e14babe527f83a26a" to Type.ALBUM)
            ),
            listOf(
                ("5dfeb08e14babe527f83a26a" to Type.ALBUM) to "https://www.hdtracks.com/album/5dfeb08e14babe527f83a26a"
            )
        ),
        AUDIUS(
            AudiusParser(), listOf(
                "https://audius.co/tracks/8EadP" to ("8EadP" to Type.SONG),
                "https://audius.co/playlists/DXr2j" to ("DXr2j" to Type.PLAYLIST)
            ),
            listOf(
                ("8EadP" to Type.SONG) to "https://audius.co/tracks/8EadP"
            )
        ),
        SEVENDIGITAL(
            SevenDigitalParser(), listOf(
                "https://us.7digital.com/artist/metro-boomin/release/metroverse-instrumental-edition-31737423" to ("31737423" to Type.ALBUM)
            ),
            listOf(
                ("31737423" to Type.ALBUM) to "https://us.7digital.com/artist/a/release/b-31737423"
            )
        ),
        OFFIZIELLECHARTS(
            OffizielleChartsParser(), listOf(
                "https://www.offiziellecharts.de/charts/album-details-538446" to ("538446" to Type.ALBUM),
                "https://www.offiziellecharts.de/titel-details-12345" to ("12345" to Type.SONG)
            ),
            listOf(
                ("538446" to Type.ALBUM) to "https://www.offiziellecharts.de/album-details-538446",
                ("12345" to Type.SONG) to "https://www.offiziellecharts.de/titel-details-12345"
            )
        ),
        LAUT(
            LautParser(), listOf(
                "https://www.laut.de/RAF-Camora/Alben/Zukunft-116597" to ("116597" to Type.ALBUM),
                "https://www.laut.de/Drake" to ("Drake" to Type.ARTIST)
            ),
            listOf(
                ("116597" to Type.ALBUM) to null,
                ("Drake" to Type.ARTIST) to "https://www.laut.de/Drake"
            )
        ),
        TIKTOK(
            TikTokParser(), listOf(
                "https://music.tiktok.com/album/123" to ("123" to Type.ALBUM),
                "https://music.tiktok.com/track/456" to ("456" to Type.SONG)
            ),
            listOf(
                ("123" to Type.ALBUM) to "https://music.tiktok.com/album/123",
                ("456" to Type.SONG) to "https://music.tiktok.com/track/456"
            )
        );
    }

    @Test
    fun `Parsers should correctly parse various URLs`() = runTest {
        for (data in ParserTestData.entries) {
            for ((url, expected) in data.testCases) {
                val result = data.parser.parse(url)
                assertEquals(expected, result, "Failed to parse $url with ${data.parser.name}")
            }
        }
    }

    @Test
    fun `Parsers should correctly generate canonical URLs`() {
        val generatedUrls = mutableListOf<String>()
        for (data in ParserTestData.entries) {
            for (case in data.toUrlCases) {
                val id = case.first.first
                val type = case.first.second
                val expected = case.second

                val result = data.parser.toUrl(id, type)
                assertEquals(expected, result, "Failed to generate URL for $id ($type) with ${data.parser.name}")
                if (result != null) {
                    generatedUrls.add("${data.parser.name} ($type): $result")
                }
            }
        }
        println("Generated URLs:\n" + generatedUrls.joinToString("\n"))
    }

    @Test
    fun `ParserFactory should return correct parser instance`() {
        val testCases = mapOf(
            "youtube:123" to YoutubeParser::class,
            "https://www.youtube.com/watch?v=123" to YoutubeParser::class,
            "soundcloud:123" to SoundcloudParser::class,
            "https://soundcloud.com/artist/song" to SoundcloudParser::class,
            "tidal:123" to TidalParser::class,
            "tiddl:123" to TidalParser::class,
            "https://tidal.com/track/123" to TidalParser::class,
            "spotify:123" to SpotifyParser::class,
            "https://open.spotify.com/track/123" to SpotifyParser::class,
            "musicbrainz:123" to MusicBrainzParser::class,
            "https://musicbrainz.org/artist/123" to MusicBrainzParser::class,
            "listenbrainz:123" to ListenBrainzParser::class,
            "https://listenbrainz.org/artist/123" to ListenBrainzParser::class,
            "amazon:123" to AmazonParser::class,
            "https://www.amazon.de/dp/B01M4OCFDH" to AmazonParser::class,
            "apple:123" to AppleMusicParser::class,
            "https://music.apple.com/us/album/123" to AppleMusicParser::class,
            "deezer:123" to DeezerParser::class,
            "https://www.deezer.com/album/123" to DeezerParser::class,
            "yandex:123" to YandexParser::class,
            "https://music.yandex.ru/album/123" to YandexParser::class,
            "pandora:123" to PandoraParser::class,
            "https://www.pandora.com/AL:123" to PandoraParser::class,
            "beatport:123" to BeatportParser::class,
            "https://www.beatport.com/release/slug/123" to BeatportParser::class,
            "boomplay:123" to BoomplayParser::class,
            "https://www.boomplay.com/albums/123" to BoomplayParser::class,
            "discogs:123" to DiscogsParser::class,
            "https://www.discogs.com/release/123" to DiscogsParser::class,
            "rateyourmusic:123" to RateYourMusicParser::class,
            "https://rateyourmusic.com/release/album/a/b/" to RateYourMusicParser::class,
            "wikidata:123" to WikidataParser::class,
            "https://www.wikidata.org/wiki/Q123" to WikidataParser::class,
            "mora:123" to MoraParser::class,
            "https://mora.jp/package/1/123/" to MoraParser::class,
            "napster:123" to NapsterParser::class,
            "https://play.napster.com/album/alb.123" to NapsterParser::class,
            "qobuz:123" to QobuzParser::class,
            "https://www.qobuz.com/album/slug/id" to QobuzParser::class,
            "anghami:123" to AnghamiParser::class,
            "https://play.anghami.com/album/123" to AnghamiParser::class,
            "livemixtapes:123" to LiveMixtapesParser::class,
            "https://www.livemixtapes.com/mixtapes/123/slug.html" to LiveMixtapesParser::class,
            "musiksammler:123" to MusikSammlerParser::class,
            "https://www.musik-sammler.de/album/123/" to MusikSammlerParser::class,
            "bandcamp:123" to BandcampParser::class,
            "https://artist.bandcamp.com" to BandcampParser::class,
            "genius:123" to GeniusParser::class,
            "https://genius.com/lyrics" to GeniusParser::class,
            "allmusic:123" to AllMusicParser::class,
            "https://www.allmusic.com/album/mw123" to AllMusicParser::class,
            "lastfm:123" to LastFmParser::class,
            "https://www.last.fm/music/artist" to LastFmParser::class,
            "datpiff:123" to DatPiffParser::class,
            "http://www.datpiff.com/mixtape.123.html" to DatPiffParser::class,
            "audiomack:123" to AudiomackParser::class,
            "https://audiomack.com/artist" to AudiomackParser::class,
            "spinrilla:123" to SpinrillaParser::class,
            "https://spinrilla.com/mixtapes/slug" to SpinrillaParser::class,
            "jaxsta:123" to JaxstaParser::class,
            "https://jaxsta.com/release/uuid" to JaxstaParser::class,
            "junodownload:123" to JunoDownloadParser::class,
            "https://www.junodownload.com/products/slug/123-02/" to JunoDownloadParser::class,
            "ototoy:123" to OtotoyParser::class,
            "https://ototoy.jp/_/default/p/123" to OtotoyParser::class,
            "hdtracks:123" to HdTracksParser::class,
            "https://www.hdtracks.com/album/123456789012345678901234" to HdTracksParser::class,
            "audius:123" to AudiusParser::class,
            "https://audius.co/handle/slug-123" to AudiusParser::class,
            "7digital:123" to SevenDigitalParser::class,
            "https://us.7digital.com/artist/a/release/b-123" to SevenDigitalParser::class,
            "offiziellecharts:123" to OffizielleChartsParser::class,
            "https://www.offiziellecharts.de/album-details-123" to OffizielleChartsParser::class,
            "laut:123" to LautParser::class,
            "https://www.laut.de/artist/Alben/slug-123" to LautParser::class
        )

        for ((input, expectedClass) in testCases) {
            val parser = ParserFactory.getParser(input)
            assertNotNull(parser, "No parser found for $input")
            assertTrue(expectedClass.isInstance(parser), "Expected ${expectedClass.simpleName} for $input, but got ${parser::class.simpleName}")
        }
        
        assertNull(ParserFactory.getParser("https://example.com"))
    }

    @Test
    fun `ParserFactory should return parser for provider name`() {
        val providers = mapOf(
            "YouTube" to YoutubeParser::class,
            "soundcloud" to SoundcloudParser::class,
            "TIDAL" to TidalParser::class,
            "tiddl" to TidalParser::class,
            "tdn" to TidalParser::class,
            "Spotify" to SpotifyParser::class,
            "MusicBrainz" to MusicBrainzParser::class,
            "ListenBrainz" to ListenBrainzParser::class,
            "amazon" to AmazonParser::class,
            "apple" to AppleMusicParser::class,
            "itunes" to AppleMusicParser::class,
            "deezer" to DeezerParser::class,
            "yandex" to YandexParser::class,
            "pandora" to PandoraParser::class,
            "beatport" to BeatportParser::class,
            "boomplay" to BoomplayParser::class,
            "discogs" to DiscogsParser::class,
            "rateyourmusic" to RateYourMusicParser::class,
            "rym" to RateYourMusicParser::class,
            "wikidata" to WikidataParser::class,
            "mora" to MoraParser::class,
            "napster" to NapsterParser::class,
            "qobuz" to QobuzParser::class,
            "anghami" to AnghamiParser::class,
            "livemixtapes" to LiveMixtapesParser::class,
            "musiksammler" to MusikSammlerParser::class,
            "bandcamp" to BandcampParser::class,
            "genius" to GeniusParser::class,
            "allmusic" to AllMusicParser::class,
            "lastfm" to LastFmParser::class,
            "datpiff" to DatPiffParser::class,
            "audiomack" to AudiomackParser::class,
            "spinrilla" to SpinrillaParser::class,
            "jaxsta" to JaxstaParser::class,
            "junodownload" to JunoDownloadParser::class,
            "ototoy" to OtotoyParser::class,
            "hdtracks" to HdTracksParser::class,
            "audius" to AudiusParser::class,
            "7digital" to SevenDigitalParser::class,
            "offiziellecharts" to OffizielleChartsParser::class,
            "laut" to LautParser::class
        )

        for ((name, expectedClass) in providers) {
            val parser = ParserFactory.getParserForProvider(name)
            assertNotNull(parser, "No parser found for provider $name")
            assertTrue(expectedClass.isInstance(parser), "Expected ${expectedClass.simpleName} for $name")
        }
        
        assertNull(ParserFactory.getParserForProvider("unknown"))
    }

    @Test
    fun `YoutubeParser should handle edge cases`() = runTest {
        val parser = YoutubeParser()
        assertNull(parser.parse("https://www.youtube.com/watch?id=123"))
        assertNull(parser.parse("https://www.youtube.com/watch"))
        assertNull(parser.parse("https://youtube.com"))
    }

    @Test
    fun `SoundcloudParser should handle edge cases`() = runTest {
        val parser = SoundcloudParser()
        assertNull(parser.parse("https://soundcloud.com"))
        assertNull(parser.parse("https://soundcloud.com/"))
        val result = parser.parse("https://soundcloud.com/a/b/c/d/e")
        assertNotNull(result)
        assertEquals("a/b/c/d/e" to Type.SONG, result)
    }
}
