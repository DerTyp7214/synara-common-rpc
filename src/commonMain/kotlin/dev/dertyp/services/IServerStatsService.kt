package dev.dertyp.services

import dev.dertyp.data.ProxyInfo
import dev.dertyp.data.ServerStats
import dev.dertyp.rpc.annotations.RpcDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Basic server health and performance monitoring.")
interface IServerStatsService {
    @RpcDoc("Retrieve detailed system performance metrics and library statistics.")
    suspend fun getStats(): ServerStats
    @RpcDoc("Simple connectivity check to verify the server is responding.")
    suspend fun health(): Boolean
    @RpcDoc("Retrieve information about the reverse proxy status and configuration.")
    suspend fun getProxyInfo(): ProxyInfo?
}