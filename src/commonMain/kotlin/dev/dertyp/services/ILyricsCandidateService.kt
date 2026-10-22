@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.RequiresCapability
import dev.dertyp.data.SongLyricsInfo
import dev.dertyp.data.UserCapability
import dev.dertyp.rpc.annotations.RestPost
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import dev.dertyp.services.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.UseContextualSerialization

@Rpc
@RpcDoc("Lyrics of songs from several providers. Every provider answer is kept as a candidate so a client can choose which version to show.")
interface ILyricsCandidateService {
    @RpcDoc("Get the lyrics a player shows. This is the default candidate or the given one, with the lyrics filled. Providers are not asked.")
    suspend fun getLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("The candidate to return instead of the default one.") candidateId: PlatformUUID? = null
    ): LyricsCandidate?

    @RpcDoc("Get the lyrics summary of songs a client already has.")
    suspend fun getLyricsInfo(
        @RpcParamDoc("The song unique identifiers.") songIds: List<PlatformUUID>
    ): Map<PlatformUUID, SongLyricsInfo>

    @RestPost
    @RpcDoc("Get the stored candidates of a song and ask the providers that have not answered yet or are due again. New answers are stored and ranked.")
    suspend fun findLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("Identifiers of the providers to ask, or null for all enabled ones.") providers: List<String>? = null,
        @RpcParamDoc("Asks every provider again. Needs the edit capability.") refresh: Boolean = false
    ): LyricsSearchResult

    @RestPost
    @RpcDoc("Ask the providers for the lyrics of any track. Nothing is stored and the candidates are not tied to a song.")
    suspend fun findLyricsByQuery(
        @RpcParamDoc("The title of the track.") title: String,
        @RpcParamDoc("The artist of the track.") artist: String,
        @RpcParamDoc("The album of the track.") album: String? = null,
        @RpcParamDoc("The duration of the track in milliseconds.") durationMs: Long? = null,
        @RpcParamDoc("Identifiers of the providers to ask, or null for all enabled ones.") providers: List<String>? = null
    ): LyricsSearchResult

    @RpcDoc("Get the stored candidates of a song. Providers are not asked.")
    suspend fun getLyricsCandidates(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("Whether to fill the lyrics of every candidate.") includeContent: Boolean = false
    ): LyricsSearchResult

    @RpcDoc("Get one candidate with the lyrics filled.")
    suspend fun getLyricsCandidate(
        @RpcParamDoc("The candidate unique identifier.") candidateId: PlatformUUID
    ): LyricsCandidate?

    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Store hand-edited synced lyrics as the manual candidate of a song. A null value removes the manual candidate.", errors = ["IllegalArgumentException"])
    suspend fun setSyncedLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("The synced lyrics, or null to remove the manual candidate.") lyrics: SyncedLyrics?
    ): LyricsSearchResult

    @RequiresCapability(UserCapability.EDIT)
    @RestPost
    @RpcDoc("Store synced lyrics parsed from a file as the manual candidate of a song.", errors = ["IllegalArgumentException"])
    suspend fun importSyncedLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("The file to import.") file: LyricsImport
    ): LyricsSearchResult

    @RequiresCapability(UserCapability.EDIT)
    @RpcDoc("Pin the default candidate of a song for everyone.")
    suspend fun setPreferredLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("The candidate to pin, or null to use the best ranked one again.") candidateId: PlatformUUID?
    ): LyricsSearchResult

    @RpcDoc("Get the enabled providers in priority order.")
    suspend fun getLyricsProviders(): List<LyricsProviderInfo>

    @RpcDoc("Ask the providers for the lyrics of a song and emit each candidate as soon as its provider answers.")
    fun streamLyrics(
        @RpcParamDoc("The song unique identifier.") songId: PlatformUUID,
        @RpcParamDoc("Identifiers of the providers to ask, or null for all enabled ones.") providers: List<String>? = null
    ): Flow<LyricsCandidate>
}
