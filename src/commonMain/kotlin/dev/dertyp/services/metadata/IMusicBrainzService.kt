package dev.dertyp.services.metadata

import dev.dertyp.PlatformUUID
import dev.dertyp.data.MusicBrainzArtist
import dev.dertyp.data.MusicBrainzRecording
import dev.dertyp.data.MusicBrainzRelease
import dev.dertyp.data.MusicBrainzReleaseGroup
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Fetch raw metadata records directly from the MusicBrainz database.")
interface IMusicBrainzService {
    @RpcDoc("Retrieve a MusicBrainz Artist record.")
    suspend fun getArtist(@RpcParamDoc("The MusicBrainz Artist UUID.") id: PlatformUUID): MusicBrainzArtist?
    @RpcDoc("Retrieve a MusicBrainz Recording record.")
    suspend fun getRecording(@RpcParamDoc("The MusicBrainz Recording UUID.") id: PlatformUUID): MusicBrainzRecording?
    @RpcDoc("Retrieve a MusicBrainz Release record.")
    suspend fun getRelease(@RpcParamDoc("The MusicBrainz Release UUID.") id: PlatformUUID): MusicBrainzRelease?
    @RpcDoc("Retrieve a MusicBrainz Release Group record.")
    suspend fun getReleaseGroup(@RpcParamDoc("The MusicBrainz Release Group UUID.") id: PlatformUUID): MusicBrainzReleaseGroup?
}
