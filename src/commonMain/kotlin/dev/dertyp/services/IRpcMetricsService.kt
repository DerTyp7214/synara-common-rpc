package dev.dertyp.services

import dev.dertyp.data.RequiresAdmin
import dev.dertyp.data.RpcCallEvent
import dev.dertyp.data.RpcCallStat
import dev.dertyp.data.RpcCallTotal
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Admin-only access to RPC call usage metrics (how often each call is invoked, per user and over time).")
interface IRpcMetricsService {
    @RequiresAdmin
    @RpcDoc("Lifetime invocation totals per call, ordered by count descending. These counters are never pruned.")
    suspend fun lifetimeTotals(
        @RpcParamDoc("Maximum number of rows to return.") limit: Int = 100,
        @RpcParamDoc("Optional username filter; null returns totals across all users.") username: String? = null,
    ): List<RpcCallTotal>

    @RequiresAdmin
    @RpcDoc("Hourly time-series of invocation counts for a specific call, oldest first.")
    suspend fun timeSeries(
        @RpcParamDoc("The RPC service (interface) name, e.g. \"ISongService\".") service: String,
        @RpcParamDoc("The RPC method (call) name.") method: String,
        @RpcParamDoc("Only include buckets at or after this epoch-millis timestamp.") sinceMillis: Long,
    ): List<RpcCallStat>

    @RequiresAdmin
    @RpcDoc("The most recent recorded invocations from the capped event log, newest first.")
    suspend fun recentEvents(
        @RpcParamDoc("Maximum number of events to return.") limit: Int = 100,
    ): List<RpcCallEvent>
}
