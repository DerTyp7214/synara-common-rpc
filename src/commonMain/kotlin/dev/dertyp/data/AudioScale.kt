package dev.dertyp.data

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The musical scale of a track.")
enum class AudioScale {
    @SerialName("major")
    Major,
    @SerialName("minor")
    Minor,
    @SerialName("majmin")
    MajMin,
    @SerialName("unknown")
    Unknown;

    companion object {
        fun fromString(value: String?): AudioScale = when (value?.lowercase()) {
            "major" -> Major
            "minor" -> Minor
            "majmin" -> MajMin
            else -> Unknown
        }
    }
}
