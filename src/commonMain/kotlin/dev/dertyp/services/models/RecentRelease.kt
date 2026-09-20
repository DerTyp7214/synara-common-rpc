@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.services.models

import dev.dertyp.PlatformDate
import dev.dertyp.PlatformUUID
import dev.dertyp.data.ReleaseSource
import dev.dertyp.data.ReleaseType
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Metadata for a recently released album or single from a followed artist.")
data class RecentRelease(
    @FieldDoc("The MusicBrainz release-group UUID, or the provider release id for non-MusicBrainz sources.")
    val releaseId: PlatformUUID,
    @FieldDoc("The internal artist unique identifier.")
    val artistId: PlatformUUID,
    @FieldDoc("The name of the artist.")
    val artistName: String,
    @FieldDoc("The title of the release.")
    val title: String,
    @Serializable(with = DateSerializer::class)
    @FieldDoc("The date the content was released.")
    val releaseDate: PlatformDate?,
    @FieldDoc("The type of release (Album, Single, etc.).")
    val type: ReleaseType,
    @FieldDoc("The release cover image unique identifier.")
    val imageId: PlatformUUID? = null,
    @FieldDoc("The blur hash of the release cover image.")
    val blurHash: String? = null,
    @FieldDoc("Collection of external URLs related to the release.")
    val links: List<String> = emptyList(),
    @FieldDoc("The internal album ID if it has been indexed.")
    val albumId: PlatformUUID? = null,
    @FieldDoc("The internal song ID if a single track has been indexed.")
    val songId: PlatformUUID? = null,
    @FieldDoc("The catalog this entry originates from.")
    val source: ReleaseSource = ReleaseSource.MusicBrainz,
    @FieldDoc("Whether the release date still lies in the future.")
    val upcoming: Boolean = false
)
