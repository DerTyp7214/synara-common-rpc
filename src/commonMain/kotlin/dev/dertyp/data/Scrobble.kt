@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("A scrobble reporting a library song the user is playing or has played.")
data class ScrobbleRequest(
    @FieldDoc("The library song being scrobbled.")
    val songId: PlatformUUID,
    @FieldDoc("When the song was played (epoch milliseconds). Defaults to the server's current time.")
    val listenedAt: Long? = null,
    @FieldDoc("How much of the song was played, in milliseconds.")
    val msPlayed: Long? = null,
)

@Serializable
@ModelDoc("The song a user is currently playing.")
data class NowPlaying(
    @FieldDoc("The song being played, with full metadata.")
    val song: UserSong,
    @FieldDoc("When playback started (epoch milliseconds).")
    val startedAt: Long,
)

@Serializable
@ModelDoc("A user's recently listened songs together with what they are currently playing.")
data class RecentListens(
    @FieldDoc("The song the user is currently playing, or null if nothing is playing.")
    val nowPlaying: NowPlaying?,
    @FieldDoc("Recently listened songs, newest first.")
    val recent: List<ListenedSong>,
)
