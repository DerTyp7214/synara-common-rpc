package dev.dertyp.services.models.tidal

import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@Serializable
data class TrackManifestsAttributes(
    val albumAudioNormalizationData: AudioNormalizationData? = null,
    val drmData: DrmData? = null,
    val formats: List<Formats>? = null,
    @property:ObjCName("hashValue") val hash: String? = null,
    val trackAudioNormalizationData: AudioNormalizationData? = null,
    val trackPresentation: TrackPresentation? = null,
    val uri: String? = null
): BaseAttributes() {
    @Suppress("unused")
    enum class Formats {
        HEAACV1,
        AACLC,
        FLAC,
        FLAC_HIRES,
        EAC3_JOC;
    }

    @Suppress("unused")
    enum class TrackPresentation {
        FULL,
        PREVIEW;
    }
}

