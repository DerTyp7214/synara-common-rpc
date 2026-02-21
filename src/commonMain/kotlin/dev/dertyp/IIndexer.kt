package dev.dertyp

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IIndexer {
    fun start(): Flow<String>
}