@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.HueBridgeCandidate
import dev.dertyp.data.HueBridgeInfo
import dev.dertyp.data.HuePairingStatus
import dev.dertyp.data.HueStatus
import dev.dertyp.data.HueTarget
import dev.dertyp.data.HueUserLink
import dev.dertyp.data.RequiresAdmin
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Philips Hue bridges paired with this server and the current user's light links, driven by now-playing changes.")
interface IHueService {
    @RestGet
    @RpcDoc("Discover Hue bridges on the local network via mDNS and the Hue cloud discovery endpoint.")
    suspend fun discoverBridges(): List<HueBridgeCandidate>

    @RestGet
    @RpcDoc("Bridges already paired with this server.")
    suspend fun listBridges(): List<HueBridgeInfo>

    @RestPost
    @RpcDoc("Pair with a bridge. Press the link button on the bridge while this flow polls for up to 30 seconds.")
    fun startPairing(@RpcParamDoc("IP address of the bridge.") ip: String): Flow<HuePairingStatus>

    @RestPost
    @RequiresAdmin
    @RpcDoc("Remove a paired bridge together with every user link to it.")
    suspend fun removeBridge(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): Boolean

    @RestGet
    @RpcDoc("Lights, rooms and zones exposed by a bridge.", errors = ["IllegalArgumentException"])
    suspend fun listTargets(@RpcParamDoc("Server-side bridge unique identifier.") bridgeId: PlatformUUID): List<HueTarget>

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
