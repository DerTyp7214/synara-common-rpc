package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.Artist
import dev.dertyp.data.MergeArtists
import dev.dertyp.data.PaginatedResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface IArtistService {
    suspend fun byId(id: PlatformUUID): Artist?
    suspend fun byIds(ids: List<PlatformUUID>): List<Artist>
    suspend fun rankedSearch(page: Int, pageSize: Int, query: String): PaginatedResponse<Artist>
    suspend fun byGroup(page: Int, pageSize: Int, groupId: PlatformUUID): PaginatedResponse<Artist>
    suspend fun mergeArtists(mergeArtists: MergeArtists): Artist?
    suspend fun allArtists(page: Int, pageSize: Int): PaginatedResponse<Artist>
}