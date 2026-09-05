@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A Hue bridge found on the network but not necessarily paired yet.")
data class HueBridgeCandidate(
    @FieldDoc("Bridge hardware id (hex), when known.")
    val bridgeId: String? = null,
    @FieldDoc("IP address of the bridge.")
    val ip: String,
    @FieldDoc("Model id, when known.")
    val modelId: String? = null,
    @FieldDoc("Whether this bridge is already paired with the server.")
    val paired: Boolean = false,
)

@Serializable
@ModelDoc("A Hue bridge paired with this server.")
data class HueBridgeInfo(
    @FieldDoc("Server-side unique identifier of the bridge.")
    val id: PlatformUUID,
    @FieldDoc("Bridge hardware id (hex).")
    val bridgeId: String,
    @FieldDoc("IP address of the bridge.")
    val ip: String,
    @FieldDoc("Display name.")
    val name: String,
    @FieldDoc("Model id, when known.")
    val modelId: String? = null,
    @FieldDoc("Unix timestamp in milliseconds of the last successful contact.")
    val lastSeen: Long? = null,
    @FieldDoc("Last error talking to the bridge, if any.")
    val lastError: String? = null,
)

@Serializable
@ModelDoc("State of a pairing attempt.")
enum class HuePairingState {
    @FieldDoc("Contacting the bridge.")
    CONNECTING,
    @FieldDoc("Waiting for the link button on the bridge to be pressed.")
    WAITING_FOR_BUTTON,
    @FieldDoc("Pairing succeeded.")
    PAIRED,
    @FieldDoc("The link button was not pressed in time.")
    TIMEOUT,
    @FieldDoc("Pairing failed.")
    ERROR
}

@Serializable
@ModelDoc("Progress of a pairing attempt.")
data class HuePairingStatus(
    @FieldDoc("Current state.")
    val state: HuePairingState,
    @FieldDoc("Human-readable detail, e.g. the error.")
    val message: String? = null,
    @FieldDoc("The paired bridge once state is PAIRED.")
    val bridge: HueBridgeInfo? = null,
)

@Serializable
@ModelDoc("Kind of light target on a bridge.")
enum class HueTargetType {
    @FieldDoc("A single light.")
    LIGHT,
    @FieldDoc("A room; controlled through its grouped light.")
    ROOM,
    @FieldDoc("A zone; controlled through its grouped light.")
    ZONE
}

@Serializable
@ModelDoc("A light, room or zone on a bridge.")
data class HueTarget(
    @FieldDoc("Kind of target.")
    val type: HueTargetType,
    @FieldDoc("Bridge resource id of the light, room or zone.")
    val id: String,
    @FieldDoc("Display name.")
    val name: String,
    @FieldDoc("Resource id of the grouped light for rooms and zones.")
    val groupedLightId: String? = null,
)

@Serializable
@ModelDoc("How strongly the lights react.")
enum class HueIntensity {
    @FieldDoc("Dim, subtle colors.")
    LOW,
    @FieldDoc("Balanced.")
    MEDIUM,
    @FieldDoc("Bright, saturated.")
    HIGH
}

@Serializable
@ModelDoc("How the transition duration between colors is chosen.")
enum class HueTransitionMode {
    @FieldDoc("Use the configured duration.")
    FIXED,
    @FieldDoc("Derive the duration from the song tempo.")
    BPM
}

@Serializable
@ModelDoc("What happens to the lights when playback stops.")
enum class HueStopMode {
    @FieldDoc("Leave the lights as they are.")
    KEEP,
    @FieldDoc("Turn the linked lights off.")
    OFF,
    @FieldDoc("Restore the state the lights had before playback started.")
    RESTORE
}

@Serializable
@ModelDoc("Ambient light movement while a song plays.")
enum class HueMotionMode {
    @FieldDoc("Set the colors once per track.")
    OFF,
    @FieldDoc("Slowly rotate the palette across the lights with long crossfades.")
    SLOW,
    @FieldDoc("Rotate the palette once per bar of the song's tempo and let brightness follow the loudness envelope.")
    TEMPO
}

@Serializable
@ModelDoc("A user's link to a bridge: which lights follow the user's playback and how.")
data class HueUserLink(
    @FieldDoc("Server-side unique identifier of the bridge.")
    val bridgeId: PlatformUUID,
    @FieldDoc("Whether the link is active.")
    val enabled: Boolean = false,
    @FieldDoc("Lights, rooms and zones that follow playback.")
    val targets: List<HueTarget> = emptyList(),
    @FieldDoc("Reaction strength.")
    val intensity: HueIntensity = HueIntensity.MEDIUM,
    @FieldDoc("Transition duration mode.")
    val transitionMode: HueTransitionMode = HueTransitionMode.FIXED,
    @FieldDoc("Transition duration in milliseconds for FIXED mode.")
    val transitionMs: Int = 400,
    @FieldDoc("Behaviour when playback stops.")
    val onStop: HueStopMode = HueStopMode.KEEP,
    @FieldDoc("Unix timestamp in milliseconds of the last change.")
    val updatedAt: Long = 0L,
    @FieldDoc("Ambient movement while a song plays.")
    val motion: HueMotionMode = HueMotionMode.OFF,
)

@Serializable
@ModelDoc("Current light state for a user's links.")
data class HueStatus(
    @FieldDoc("Unix timestamp in milliseconds of the last command sent for the user, if any.")
    val lastCommandAt: Long? = null,
    @FieldDoc("Last error for any of the user's bridges, if any.")
    val lastError: String? = null,
    @FieldDoc("Colors (ARGB) most recently applied, in target order.")
    val currentColors: List<Int> = emptyList(),
)
