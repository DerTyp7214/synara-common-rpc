package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import dev.dertyp.services.ISyncService
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class FavSync(
    val userId: @Contextual UUID,
    val service: ISyncService.SyncServiceType,
    @Serializable(with = DateSerializer::class)
    val syncedAt: Date,
)
