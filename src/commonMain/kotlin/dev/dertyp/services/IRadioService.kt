package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.RadioSeed
import dev.dertyp.data.RadioType
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Infinite radio stations seeded from listen history, a seed selection, or pure randomness.")
interface IRadioService {
    @RpcDoc("Create a radio session and return its identifier. The same identifier can be reused to resume the station where it left off.")
    suspend fun createRadioSession(
        @RpcParamDoc("The strategy used to seed the station. Ignored when a seed is provided.") type: RadioType = RadioType.RANDOM,
        @RpcParamDoc("Optional seed material; when set, the station plays songs similar to the seed.") seed: RadioSeed? = null,
    ): PlatformUUID

    @RpcDoc("Infinite stream of song identifiers for a radio session. Re-collecting the same session continues without repeating songs.")
    fun radioFlow(
        @RpcParamDoc("The radio session identifier, as returned by createRadioSession.") sessionId: PlatformUUID
    ): Flow<PlatformUUID>
}
