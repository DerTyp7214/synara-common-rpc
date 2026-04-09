@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import dev.dertyp.services.ISyncService
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Record of a successful favorite synchronization task.")
data class FavSync(
    @FieldDoc("The user who initiated the synchronization.")
    val userId: PlatformUUID,
    @FieldDoc("The external service that was synchronized (e.g., TIDAL).")
    val service: ISyncService.SyncServiceType,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("Timestamp of when the synchronization was completed.")
    val syncedAt: PlatformDate,
)
