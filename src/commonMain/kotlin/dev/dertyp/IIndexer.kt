package dev.dertyp

import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Local file system media scanning.")
interface IIndexer {
    @RpcDoc("Start indexing files in the media library and stream progress updates.")
    fun start(): Flow<String>
}