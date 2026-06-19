package dev.dertyp.services.models.tidal


import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistUpdateOperationPayloadDataAttributes(
    val accessType: AccessType? = null,
    @property:ObjCName("playlistDescription") val description: String? = null,
    val name: String? = null
): BaseAttributes() {
    @Suppress("unused")
    enum class AccessType {
        PUBLIC,
        UNLISTED;
    }
}

