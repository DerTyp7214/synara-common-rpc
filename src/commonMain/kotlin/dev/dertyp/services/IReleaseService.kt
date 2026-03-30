@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.services.models.FollowedArtist
import dev.dertyp.services.models.RecentRelease
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
interface IReleaseService {
    suspend fun followArtist(musicBrainzId: String): Boolean
    suspend fun unfollowArtist(artistId: PlatformUUID): Boolean
    suspend fun getFollowedArtists(): List<FollowedArtist>
    suspend fun getRecentReleases(): List<RecentRelease>
}
