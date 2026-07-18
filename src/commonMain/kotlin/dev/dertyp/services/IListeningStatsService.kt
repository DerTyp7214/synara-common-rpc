package dev.dertyp.services

import dev.dertyp.data.ListeningStats
import dev.dertyp.data.StatsRange
import dev.dertyp.rpc.annotations.RestGet
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
        @RpcParamDoc("Maximum entries per top list; clamped to 1..100.") topLimit: Int,
    ): ListeningStats
}
