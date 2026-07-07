package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.MoodSummary
import dev.dertyp.data.RecommendationWindow
import dev.dertyp.data.UserSong
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Personalized music recommendations from the learned embedding space.")
interface IRecommendationService {
    @RpcDoc("Songs most similar to the given seed songs in the learned embedding space.")
    suspend fun getSimilarSongs(
        @RpcParamDoc("Seed song unique identifiers.") seedSongIds: List<PlatformUUID>,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 20
    ): List<UserSong>

    @RpcDoc("A personalized mix seeded from the user's recent listens over a time window.")
    suspend fun getMix(
        @RpcParamDoc("Recency window used to seed the mix.") window: RecommendationWindow = RecommendationWindow.WEEK,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 50
    ): List<UserSong>

    @RpcDoc("Songs belonging to a given mood cluster.")
    suspend fun getMoodPlaylist(
        @RpcParamDoc("The mood label, as returned by getMoods.") mood: String,
        @RpcParamDoc("The maximum number of results to return.") limit: Int = 50
    ): List<UserSong>

    @RpcDoc("Available mood labels with their song counts.")
    suspend fun getMoods(): List<MoodSummary>
}
