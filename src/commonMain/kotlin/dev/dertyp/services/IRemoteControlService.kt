package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.ClientRequestStatus
import dev.dertyp.data.PlaybackCommand
import dev.dertyp.data.RemotePlaybackStatus
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Controls playback on another device of the same user and watches what it is doing. Only devices that are connected to the request channel offering " +
        "the remote control capability can be reported on or addressed, and a device is always addressed by its session. Nothing here is stored: a status " +
        "lives as long as the device stays connected."
)
interface IRemoteControlService {
    @RpcDoc(
        "Publish what the calling session is playing, which every device watching it receives. A client reports after every change and every few seconds " +
            "while it is playing, so watchers can follow the position. The calling session must be connected offering remote control.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun reportStatus(
        @RpcParamDoc("What this device is playing right now. The report timestamp is filled in by the server.") status: RemotePlaybackStatus
    )

    @RpcDoc(
        "Read the last status another device of the user reported, or null when it has not reported anything since it connected.",
        errors = ["IllegalArgumentException", "UnauthorizedException"]
    )
    suspend fun getStatus(
        @RpcParamDoc("The session unique identifier of the device to read.") sessionId: PlatformUUID
    ): RemotePlaybackStatus?

    @RestGet
    @RpcDoc(
        "Watch what another device of the user is playing. The last reported status is replayed immediately, followed by every later report.",
        errors = ["IllegalArgumentException", "UnauthorizedException"]
    )
    fun observeStatus(
        @RpcParamDoc("The session unique identifier of the device to watch.") sessionId: PlatformUUID
    ): Flow<RemotePlaybackStatus>

    @RpcDoc(
        "Send a transport command to another device of the user and wait for it to answer. The device must be connected offering remote control, and " +
            "a volume command additionally requires the remote volume capability and a volume between 0 and 1. A device that stays silent is reported " +
            "as timed out after ten seconds.",
        errors = ["IllegalArgumentException", "UnauthorizedException"]
    )
    suspend fun sendCommand(
        @RpcParamDoc("The session unique identifier of the device to control.") sessionId: PlatformUUID,
        @RpcParamDoc("The transport command to apply on that device.") command: PlaybackCommand
    ): ClientRequestStatus
}
