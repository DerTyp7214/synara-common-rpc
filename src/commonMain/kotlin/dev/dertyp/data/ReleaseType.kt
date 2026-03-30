package dev.dertyp.data

import kotlinx.serialization.Serializable

@Serializable
enum class ReleaseType {
    ALBUM,
    SINGLE,
    EP,
    BROADCAST,
    OTHER,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ReleaseType = when (value?.lowercase()) {
            "album" -> ALBUM
            "single" -> SINGLE
            "ep" -> EP
            "broadcast" -> BROADCAST
            "other" -> OTHER
            else -> UNKNOWN
        }
    }
}
