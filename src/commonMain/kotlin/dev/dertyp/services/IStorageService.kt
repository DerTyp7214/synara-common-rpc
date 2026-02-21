package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc

@Rpc
interface IStorageService {
    suspend fun getTotalStorage(): Long
}