@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.serializers.DateSerializer
import dev.dertyp.services.ISyncService
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
data class FavSync(
    val userId: PlatformUUID,
    val service: ISyncService.SyncServiceType,
    @Serializable(with = DateSerializer::class)
    val syncedAt: PlatformDate,
)
