package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.PaginatedResponse
import dev.dertyp.data.TimecodeTag
import dev.dertyp.data.TimecodeTagInput
import dev.dertyp.data.TimecodeTagType
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RestPut
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Stores the tags a user attaches to a song at a position in milliseconds, either as a chapter that names a section, as a marker that points " +
        "at a single moment or as a free-form note. Tags are private to the user who created them, so they are never visible to anyone else, " +
        "and a tag disappears together with the song it belongs to. The text of a tag holds at most 1000 characters and a user keeps at most " +
        "500 tags on a single song."
)
interface ITimecodeTagService {
    @RpcDoc(
        "Create a single tag on a song. The position must not be negative and an end position, if one is given, must not lie before the position " +
            "the tag starts at.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun createTag(
        @RpcParamDoc("The song to attach the tag to.") songId: PlatformUUID,
        @RpcParamDoc("How the tag is meant to be read by a client.") tagType: TimecodeTagType,
        @RpcParamDoc("The text of the tag. May be blank.") text: String = "",
        @RpcParamDoc("Position in the song in milliseconds at which the tag starts.") timestampMs: Long,
        @RpcParamDoc("Position in the song in milliseconds at which the tag ends, or null for a tag that marks a single point.") endMs: Long? = null
    ): TimecodeTag

    @RestGet
    @RpcDoc("Read the tags the user attached to a song, ordered by the position they start at.")
    suspend fun getTags(
        @RpcParamDoc("The song to read the tags of.") songId: PlatformUUID
    ): List<TimecodeTag>

    @RpcDoc(
        "Overwrite a single tag of the user with new values. The call fails if the tag does not exist or belongs to another user.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun updateTag(
        @RpcParamDoc("The tag to change.") tagId: PlatformUUID,
        @RpcParamDoc("How the tag is meant to be read by a client.") tagType: TimecodeTagType,
        @RpcParamDoc("The text of the tag. May be blank.") text: String = "",
        @RpcParamDoc("Position in the song in milliseconds at which the tag starts.") timestampMs: Long,
        @RpcParamDoc("Position in the song in milliseconds at which the tag ends, or null for a tag that marks a single point.") endMs: Long? = null
    ): TimecodeTag

    @RpcDoc("Delete a single tag of the user. Returns false if the tag does not exist or belongs to another user.")
    suspend fun deleteTag(
        @RpcParamDoc("The tag to delete.") tagId: PlatformUUID
    ): Boolean

    @RestPut
    @RpcDoc(
        "Replace every tag the user has on a song with the supplied ones. The write is all-or-nothing, so either all tags are stored or none of " +
            "them are, and an empty list clears the tags of the song.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun replaceTags(
        @RpcParamDoc("The song whose tags are replaced.") songId: PlatformUUID,
        @RpcParamDoc("The tags to store for the song. An empty list removes every tag of the song.") tags: List<TimecodeTagInput>
    ): List<TimecodeTag>

    @RestGet
    @RpcDoc("Read the tags of the user across all songs, newest first, optionally narrowed down to a single kind of tag.")
    suspend fun listTags(
        @RpcParamDoc("Only return tags of this kind, or null for every kind.") tagType: TimecodeTagType? = null,
        @RpcParamDoc("Page index.") page: Int = 0,
        @RpcParamDoc("Number of tags per page, at most 500.") pageSize: Int = 100
    ): PaginatedResponse<TimecodeTag>
}
