package dev.dertyp.services.metadata

import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Find links to a song or album on other streaming platforms.")
interface ILinkResolverService {
    @RestGet
    @RpcDoc(
        "Resolve links to the same song or album on other platforms without touching the library. " +
            "The ISRC is tried first, then the UPC, then each seed URL in order. " +
            "Returns an empty list when nothing resolves."
    )
    suspend fun resolvePlatformLinks(
        @RpcParamDoc("Links to the song or album on a streaming platform.") urls: List<String> = emptyList(),
        @RpcParamDoc("The ISRC of the song.") isrc: String? = null,
        @RpcParamDoc("The UPC or EAN barcode of the album.") upc: String? = null
    ): List<String>
}
