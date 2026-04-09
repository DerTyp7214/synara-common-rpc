package dev.dertyp.data

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The classification of a music release.")
enum class ReleaseType {
    Album,
    Single,
    EP,
    Broadcast,
    Other,
    Unknown;

    companion object {
        fun fromString(value: String?): ReleaseType = when (value?.lowercase()) {
            "album" -> Album
            "single" -> Single
            "ep" -> EP
            "broadcast" -> Broadcast
            "other" -> Other
            else -> Unknown
        }
    }
}
