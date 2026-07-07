package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The status of a user's ListenBrainz connection.")
data class ListenBrainzStatus(
    @FieldDoc("The linked ListenBrainz username.")
    val username: String,
    @FieldDoc("Whether automatic syncing is enabled.")
    val enabled: Boolean,
    @FieldDoc("Timestamp (unix seconds) of the most recent synced listen.")
    val lastListenedAt: Long?,
    @FieldDoc("When the account was last synced (epoch milliseconds).")
    val lastSyncedAt: Long?,
    @FieldDoc("Number of the account's listens matched to local songs.")
    val matchedListenCount: Long
)
