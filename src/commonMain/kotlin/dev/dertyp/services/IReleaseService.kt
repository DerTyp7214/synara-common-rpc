@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RestDelete
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RestPublic
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
    @RestDelete
    @RpcDoc("Unfollow an artist and stop tracking their releases.")
    suspend fun unfollowArtist(@RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID): Boolean
    @RpcDoc("Get a list of all artists the current user is following.")
    suspend fun getFollowedArtists(): List<FollowedArtist>
    @RpcDoc("Retrieve a feed of recent and upcoming music releases from followed artists, merged from MusicBrainz and the Apple Music catalog. Editions of one release are folded into a single entry carrying the others in versions. Paging applies per release type: one page holds up to pageSize entries of every type, a folded group counting once, in one list sorted by date.")
    suspend fun getRecentReleases(
        @RpcParamDoc("Page index, applied to every release type separately.") page: Int = 0,
        @RpcParamDoc("Number of entries per page for each release type.") pageSize: Int = 50
    ): PaginatedResponse<RecentRelease>

    @RpcDoc("Retrieve recent music releases for a specific artist. Editions of one release are folded into a single entry carrying the others in versions.")
    suspend fun getArtistRecentReleases(
        @RpcParamDoc("The artist unique identifier.") artistId: PlatformUUID,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 150,
        @RpcParamDoc("Include entries that have been hidden from the feed; they carry hidden = true.") includeHidden: Boolean = false
    ): PaginatedResponse<RecentRelease>

    @RpcDoc("Retrieve recent music releases for an artist by their MusicBrainz ID.")
    suspend fun getRecentReleasesByMusicBrainzId(
        @RpcParamDoc("The MusicBrainz Artist UUID.") musicBrainzId: PlatformUUID,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of items per page.") pageSize: Int = 150
    ): PaginatedResponse<RecentRelease>

    @RestPublic
    @RpcDoc("Retrieve the cover image for a recent release, served from local storage when persisted or proxied from the Cover Art Archive on demand.")
    suspend fun getReleaseImage(
        @RpcParamDoc("The MusicBrainz release-group UUID of the recent release, or the provider release id for non-MusicBrainz sources.") releaseId: PlatformUUID,
        @RpcParamDoc("Requested image size (width/height). 0 for original size.") size: Int = 0
    ): ByteArray?

    @RequiresCapability(UserCapability.EDIT)
    @RestPost
    @RpcDoc("Kick off a background refresh of the cached metadata, provider links and cover image for a single recent release, returning immediately.")
    suspend fun refreshRecentRelease(
        @RpcParamDoc("The MusicBrainz release-group UUID of the recent release to refresh, or the provider release id for non-MusicBrainz sources.") releaseId: PlatformUUID
    )

    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc(
        "Hide an entry of the release feed for every user, or show it again. With includeRelated the other Apple Music entries of the same artist sharing the entry's copyright holder (or record label when no holder is known) are hidden or shown as well, and the holder or label is recorded as blocked (or unblocked) for the artist so future catalog runs hide matching entries automatically. Returns the number of entries whose visibility changed. The change applies to the whole display group, i.e. every edition the feed folds under one entry.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun setReleaseHidden(
        @RpcParamDoc("The MusicBrainz release-group UUID of the recent release, or the provider release id for non-MusicBrainz sources.") releaseId: PlatformUUID,
        @RpcParamDoc("true to hide the entry, false to show it again.") hidden: Boolean,
        @RpcParamDoc("Also apply the change to the artist's Apple Music entries sharing the copyright holder or record label and record or clear the block. Ignored for MusicBrainz entries.") includeRelated: Boolean = false
    ): Int

    @RequiresCapability(UserCapability.EDIT)
    @RestPost
    @RpcDoc(
        "Confirm that a provider entry flagged as possibly belonging to another artist is correct: clears the flag and records the entry's copyright holder, record label and ISRC registrants as known sources of the artist so future catalog runs do not flag matching entries again. Returns the updated entry.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun confirmRelease(
        @RpcParamDoc("The provider release id of the flagged entry.") releaseId: PlatformUUID
    ): RecentRelease
}
