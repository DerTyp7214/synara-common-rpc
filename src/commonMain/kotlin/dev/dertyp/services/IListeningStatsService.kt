package dev.dertyp.services

import dev.dertyp.data.LinkUnmatchedTrackRequest
import dev.dertyp.data.LinkUnmatchedTrackResult
import dev.dertyp.data.ListeningStats
import dev.dertyp.data.StatsRange
import dev.dertyp.data.TopOrder
import dev.dertyp.data.UserSong
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Listening statistics over the current user's unified listen history (local scrobbles and ListenBrainz imports), deduplicated across sources.")
interface IListeningStatsService {
    @RestGet
    @RpcDoc("Compute listen count, previous-range comparison, top songs/artists/albums, listen clock, streaks, unique counts and new discoveries for a time range.")
    suspend fun getStats(
        @RpcParamDoc("The time range to aggregate.") range: StatsRange,
        @RpcParamDoc("IANA timezone for range boundaries, e.g. Europe/Berlin. Invalid or blank falls back to UTC.") timezone: String,
        @RpcParamDoc("Maximum entries per top list. Clamped to 1..100.") topLimit: Int,
        @RpcParamDoc("Metric used to rank the top and discovery lists.") topOrder: TopOrder = TopOrder.LISTEN_COUNT,
    ): ListeningStats

    @RestGet
    @RpcDoc("Return the current user's most played songs in a time range as a playable list, ranked by the given metric. Songs that are not in the library are left out.")
    suspend fun getTopSongs(
        @RpcParamDoc("The time range to aggregate.") range: StatsRange,
        @RpcParamDoc("IANA timezone for range boundaries, e.g. Europe/Berlin. Invalid or blank falls back to UTC.") timezone: String,
        @RpcParamDoc("Maximum number of songs. Clamped to 1..500.") limit: Int,
        @RpcParamDoc("Metric used to rank the songs.") order: TopOrder = TopOrder.LISTENED_MS,
    ): List<UserSong>

    @RestPost
    @RpcDoc("Link the current user's unmatched listens of a track to a library song, identified by recording MSID and/or MBID. The link persists, applies to future synced listens, and is submitted to ListenBrainz as a manual mapping when the song has a recording MBID.")
    suspend fun linkUnmatchedTrack(
        @RpcParamDoc("The unmatched track identity and the target library song.") request: LinkUnmatchedTrackRequest,
    ): LinkUnmatchedTrackResult
}
