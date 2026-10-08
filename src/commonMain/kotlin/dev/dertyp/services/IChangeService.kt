package dev.dertyp.services

import dev.dertyp.data.Change
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Tells a client which parts of the user's state changed, so it reads them again with the regular getters instead of polling " +
        "or keeping one stream open per screen."
)
interface IChangeService {
    @RestGet
    @RpcDoc(
        "Watch the state of the user for changes. The stream stays silent until something changes and then emits the @ChangeTopic that changed, " +
            "which names the getter to call again. Bursts of changes to the same topic within a short window arrive as a single change. " +
            "Nothing is replayed, so a client reads the current state after every (re)subscribe."
    )
    fun observeChanges(): Flow<Change>
}
