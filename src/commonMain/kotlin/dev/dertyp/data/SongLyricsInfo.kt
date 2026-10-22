@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.services.models.LyricsSyncLevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Summary of the lyrics available for a song.")
data class SongLyricsInfo(
    @FieldDoc("Whether the song has any lyrics at all.")
    val available: Boolean,
    @FieldDoc("The best sync level of the default lyrics, or null if there are none.")
    val syncLevel: LyricsSyncLevel?,
    @FieldDoc("Every sync level that at least one lyrics candidate offers.")
    val levels: Set<LyricsSyncLevel>,
    @FieldDoc("Whether the song is instrumental.")
    val instrumental: Boolean = false,
    @FieldDoc("The identifier of the default lyrics candidate.")
    val defaultCandidateId: PlatformUUID? = null,
    @FieldDoc("The identifier of the provider of the default lyrics candidate.")
    val defaultProvider: String? = null,
    @FieldDoc("Number of stored lyrics candidates.")
    val candidateCount: Int = 0
)
