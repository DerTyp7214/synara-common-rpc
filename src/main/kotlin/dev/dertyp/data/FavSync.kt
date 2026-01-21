package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import dev.dertyp.services.ISyncService
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FavSync(
    @Contextual
    val userId: UUID,
    val service: ISyncService.SyncServiceType,
    @Serializable(with = DateSerializer::class)
    val syncedAt: Date,
)
