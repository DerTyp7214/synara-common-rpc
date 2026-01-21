@file:UseContextualSerialization(UUID::class)

package dev.dertyp.data

import dev.dertyp.serializers.DateSerializer
import dev.dertyp.services.ISyncService
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.util.*

@Serializable
data class FavSync(
    val userId: UUID,
    val service: ISyncService.SyncServiceType,
    @Serializable(with = DateSerializer::class)
    val syncedAt: Date,
)
