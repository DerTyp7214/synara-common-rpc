package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Supported audio formats for transcoding.")
enum class AudioFormat {
    @FieldDoc("Opus audio codec in Ogg container.") OPUS,
    @FieldDoc("Advanced Audio Coding in MP4 container.") AAC
}

@Serializable
@ModelDoc("Represents a transcoded version of a song.")
data class TranscodedVersion(
    @FieldDoc("The bitrate of the transcoded version in kbps.")
    val bitrate: Int,
    @FieldDoc("The audio format of the transcoded version.")
    val format: AudioFormat
)
