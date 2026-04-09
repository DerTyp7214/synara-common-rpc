package dev.dertyp.services

import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Search for track lyrics using external providers.")
interface ILyricsSearch {
    @RpcDoc("Search for lyrics text on external services.", errors = ["RuntimeException"])
    suspend fun searchLyrics(
        @RpcParamDoc("The name of the artist.") artist: String,
        @RpcParamDoc("The title of the song.") title: String,
        @RpcParamDoc("Whether to only return time-synced lyrics.") syncedOnly: Boolean = true
    ): List<String>
}