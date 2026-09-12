@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Determines how a timecode tag is meant to be read by a client, so a player can render sections, cue points and remarks differently.")
enum class TimecodeTagType {
    @FieldDoc("A named section of a song, usually with an end position.") CHAPTER,

    @FieldDoc("A single point of interest, for example a drop or a cue point.") MARKER,

    @FieldDoc("A free-form remark the user left at a position.") NOTE
}

@Serializable
@ModelDoc(
    "A tag a user attached to a song at a position in milliseconds. Tags are private to the user who created them and are removed together " +
        "with the song they belong to."
)
data class TimecodeTag(
    @FieldDoc("The unique identifier of the tag.")
    val id: PlatformUUID,
    @FieldDoc("The user the tag belongs to. Tags are never visible to other users.")
    val userId: PlatformUUID,
    @FieldDoc("The song the tag is attached to.")
    val songId: PlatformUUID,
    @FieldDoc("How the tag is meant to be read by a client.")
    val type: TimecodeTagType,
    @FieldDoc("The text of the tag, for example the name of a chapter or the remark of a note. May be blank.")
    val text: String = "",
    @FieldDoc("Position in the song in milliseconds at which the tag starts.")
    val timestampMs: Long,
    @FieldDoc("Position in the song in milliseconds at which the tag ends, or null for a tag that marks a single point.")
    val endMs: Long? = null,
    @FieldDoc("Unix timestamp in milliseconds at which the tag was created.")
    val createdAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last change to the tag.")
    val updatedAt: Long
)

@Serializable
@ModelDoc("A tag to store for a song, without the fields the server assigns itself. Used to write a whole set of tags of a song at once.")
data class TimecodeTagInput(
    @FieldDoc("How the tag is meant to be read by a client.")
    val type: TimecodeTagType,
    @FieldDoc("The text of the tag, for example the name of a chapter or the remark of a note. May be blank.")
    val text: String = "",
    @FieldDoc("Position in the song in milliseconds at which the tag starts.")
    val timestampMs: Long,
    @FieldDoc("Position in the song in milliseconds at which the tag ends, or null for a tag that marks a single point.")
    val endMs: Long? = null
)
