package dev.dertyp.services.models

import dev.dertyp.PlatformDate
import dev.dertyp.serializers.DateSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val numberOfVolumes: Long,
    val numberOfItems: Long,
    val duration: Long,
    val explicit: Boolean,
    @Serializable(with = DateSerializer::class)
    val releaseDate: PlatformDate,
    val copyright: String,
    val coverUrl: String,
)
