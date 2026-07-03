package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Lifetime invocation count for a single RPC call, per user. This counter only ever increases.")
data class RpcCallTotal(
    @FieldDoc("The RPC service (interface) name, e.g. \"ISongService\".")
    val service: String,
    @FieldDoc("The RPC method (call) name.")
    val method: String,
    @FieldDoc("The username that made the calls, or null for unauthenticated/public calls.")
    val username: String?,
    @FieldDoc("Total number of times this call was invoked over the lifetime of the server.")
    val count: Long,
)

@Serializable
@ModelDoc("Invocation count for a single RPC call within one hourly time bucket.")
data class RpcCallStat(
    @FieldDoc("The RPC service (interface) name.")
    val service: String,
    @FieldDoc("The RPC method (call) name.")
    val method: String,
    @FieldDoc("The username that made the calls, or null for unauthenticated/public calls.")
    val username: String?,
    @FieldDoc("Start of the hourly bucket as epoch milliseconds (truncated to the hour).")
    val bucketStart: Long,
    @FieldDoc("Number of invocations in this bucket.")
    val count: Long,
)

@Serializable
@ModelDoc("A single recorded RPC invocation from the capped recent-events log.")
data class RpcCallEvent(
    @FieldDoc("The RPC service (interface) name.")
    val service: String,
    @FieldDoc("The RPC method (call) name.")
    val method: String,
    @FieldDoc("The username that made the call, or null for unauthenticated/public calls.")
    val username: String?,
    @FieldDoc("Timestamp of the invocation as epoch milliseconds.")
    val timestamp: Long,
)
