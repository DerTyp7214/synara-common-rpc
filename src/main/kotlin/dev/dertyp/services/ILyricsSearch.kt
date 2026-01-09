package dev.dertyp.services

import kotlinx.rpc.annotations.Rpc

@Rpc
interface ILyricsSearch {
    suspend fun searchLyrics(
        artist: String,
        title: String,
        syncedOnly: Boolean = true,
        onLineReceived: suspend (String) -> Unit = {}
    ): List<String>
}