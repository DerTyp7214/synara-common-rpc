package dev.dertyp.services

import dev.dertyp.PlatformDate
import dev.dertyp.serializers.DateSerializer
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Rpc
interface ISyncService {
    interface Token {
        val scope: String?
        val accessToken: String
        val refreshToken: String?
        val expiresIn: Int
        val tokenType: String
        val userId: Long
        val createdAt: Long?
    }

    @Suppress("EnumEntryName")
    @Serializable
    enum class SyncServiceType {
        tidal,
        unknown
    }

    @Serializable
    data class Me(
        val id: String,
        val username: String,
        val email: String,
    )

    @Serializable
    data class LikedSong(
        val id: String,
        val title: String,
        @Serializable(with = DateSerializer::class)
        val addedAt: PlatformDate,
        val explicit: Boolean,
    )
}