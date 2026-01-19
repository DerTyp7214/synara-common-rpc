package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc

@Rpc
interface ILyricsSearch {
    suspend fun searchLyrics(
        artist: String,
        title: String,
        syncedOnly: Boolean = true
    ): List<String>
}