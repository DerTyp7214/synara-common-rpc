package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
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
