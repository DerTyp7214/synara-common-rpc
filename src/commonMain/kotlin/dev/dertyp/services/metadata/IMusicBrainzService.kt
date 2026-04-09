package dev.dertyp.services.metadata

import dev.dertyp.PlatformUUID
import dev.dertyp.data.*
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IMusicBrainzService {
    suspend fun getArtist(id: PlatformUUID): MusicBrainzArtist?
    suspend fun getRecording(id: PlatformUUID): MusicBrainzRecording?
    suspend fun getRelease(id: PlatformUUID): MusicBrainzRelease?
    suspend fun getReleaseGroup(id: PlatformUUID): MusicBrainzReleaseGroup?
}
