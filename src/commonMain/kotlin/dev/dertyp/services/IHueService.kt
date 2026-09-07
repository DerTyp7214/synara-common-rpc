@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.HueBridgeCandidate
import dev.dertyp.data.HueBridgeInfo
import dev.dertyp.data.HuePairingStatus
import dev.dertyp.data.HueScene
import dev.dertyp.data.HueStatus
import dev.dertyp.data.HueTarget
import dev.dertyp.data.HueUserLink
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Philips Hue bridges paired by the current user and their light links, driven by now-playing changes.")
interface IHueService {
    @RestGet
    @RpcDoc("Discover Hue bridges on the local network via mDNS and the Hue cloud discovery endpoint; paired is relative to the current user.")
    suspend fun discoverBridges(): List<HueBridgeCandidate>

    @RestGet
    @RpcDoc("Bridges paired by the current user.")
    suspend fun listBridges(): List<HueBridgeInfo>

    @RestPost
    @RpcDoc("Pair with a bridge. Press the link button on the bridge while this flow polls for up to 30 seconds. The bridge is stored for the current user; other users pair it separately.")
    fun startPairing(@RpcParamDoc("IP address of the bridge.") ip: String): Flow<HuePairingStatus>

    @RestPost
    @RpcDoc("Remove one of the current user's bridges together with the user's link to it.")
    suspend fun removeBridge(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): Boolean

    @RestGet
    @RpcDoc("Lights, rooms and zones exposed by a bridge.", errors = ["IllegalArgumentException"])
    suspend fun listTargets(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): List<HueTarget>

    @RestGet
    @RpcDoc("Scenes exposed by a bridge, each scoped to a room or zone that the bridge reports.", errors = ["IllegalArgumentException"])
    suspend fun listScenes(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): List<HueScene>

    @RestGet
    @RpcDoc("The current user's light links.")
    suspend fun getLinks(): List<HueUserLink>

    @RestPost
    @RpcDoc("Create or update the current user's link to a bridge.", errors = ["IllegalArgumentException"])
    suspend fun setLink(@RpcParamDoc("Link settings; bridgeId selects the bridge.") link: HueUserLink): HueUserLink

    @RestPost
    @RpcDoc("Remove the current user's link to a bridge.")
    suspend fun removeLink(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): Boolean

    @RestPost
    @RpcDoc("Flash the given targets with a test palette.", errors = ["IllegalArgumentException"])
    suspend fun test(
        @RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID,
        @RpcParamDoc("Targets to test.") targets: List<HueTarget>,
    ): Boolean

    @RestGet
    @RpcDoc("Last command time, last error and current colors for the current user's links.")
    suspend fun status(): HueStatus
}
