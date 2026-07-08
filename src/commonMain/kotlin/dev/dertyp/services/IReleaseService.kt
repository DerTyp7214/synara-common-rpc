@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.services.models.FollowedArtist
import dev.dertyp.services.models.RecentRelease
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Track and receive notifications for new music releases.")
interface IReleaseService {
    @RpcDoc("Follow an artist by their MusicBrainz ID to track their releases.")
    suspend fun followArtist(@RpcParamDoc("The MusicBrainz Artist UUID.") musicBrainzId: PlatformUUID): Boolean
    @RpcDoc("Unfollow an artist and stop tracking their releases.")
    suspend fun unfollowArtist(@RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID): Boolean
    @RpcDoc("Get a list of all artists the current user is following.")
    suspend fun getFollowedArtists(): List<FollowedArtist>
    @RpcDoc("Retrieve a feed of recent music releases from followed artists.")
    suspend fun getRecentReleases(
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 150
    ): PaginatedResponse<RecentRelease>

    @RpcDoc("Retrieve recent music releases for a specific artist.")
    suspend fun getArtistRecentReleases(
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 150
    ): PaginatedResponse<RecentRelease>

    @RpcDoc("Retrieve recent music releases for an artist by their MusicBrainz ID.")
    suspend fun getRecentReleasesByMusicBrainzId(
        @RpcParamDoc("The MusicBrainz Artist UUID.") musicBrainzId: PlatformUUID,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 150
    ): PaginatedResponse<RecentRelease>

    @RequiresCapability(UserCapability.EDIT)
    @RestPost
    @RpcDoc("Kick off a background refresh of the cached metadata, provider links and cover image for a single recent release, returning immediately.")
    suspend fun refreshRecentRelease(
        @RpcParamDoc("The MusicBrainz release-group UUID of the recent release to refresh.") releaseId: PlatformUUID
    )
}
