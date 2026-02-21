package dev.dertyp.services.models.tidal


import dev.dertyp.PlatformUUID
import dev.dertyp.serializers.UUIDSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PlayQueuesFutureResourceIdentifierMeta(
    @Serializable(with = UUIDSerializer::class)
    val batchId: PlatformUUID,
    val itemId: String
)