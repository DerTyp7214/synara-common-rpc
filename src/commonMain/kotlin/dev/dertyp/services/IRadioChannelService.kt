package dev.dertyp.services

import dev.dertyp.PlatformUUID
import dev.dertyp.data.InsertableRadioChannel
import dev.dertyp.data.RadioChannel
import dev.dertyp.data.RadioChannelItemType
import dev.dertyp.data.RadioChannelSearchResults
import dev.dertyp.data.RequiresAdmin
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc("Admin-curated radio channels: browse and play for everyone, create and edit for admins.")
interface IRadioChannelService {
    @RpcDoc("List radio channels. Non-admin users only see published channels.")
    suspend fun listChannels(): List<RadioChannel>

    @RpcDoc("Get a radio channel by ID. Returns null for a draft channel when the caller is not an admin.")
    suspend fun getChannel(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID
    ): RadioChannel?

    @RpcDoc("Ranked search across the songs, artists and albums configured on a channel. An empty query lists all configured content.")
    suspend fun rankedSearch(
        @RpcParamDoc("The channel unique identifier.") channelId: PlatformUUID,
        @RpcParamDoc("The search query; empty to list all configured content.") query: String = "",
        @RpcParamDoc("Whether to include explicit-content songs.") explicit: Boolean = false,
        @RpcParamDoc("The page index (starting from 0).") page: Int = 0,
        @RpcParamDoc("Items per page, applied per result type.") pageSize: Int = 50,
    ): RadioChannelSearchResults

    @RpcDoc("Start playing a channel. Returns a radio session id to feed into IRadioService.radioFlow.")
    suspend fun startChannel(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID
    ): PlatformUUID

    @RequiresAdmin
    @RpcDoc("Create a new radio channel.")
    suspend fun createChannel(
        @RpcParamDoc("The initial channel metadata.") channel: InsertableRadioChannel
    ): PlatformUUID

    @RequiresAdmin
    @RpcDoc("Update a channel's metadata (name, description, enabled, position, discovery).")
    suspend fun updateChannel(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The new metadata.") channel: InsertableRadioChannel
    ): Boolean

    @RequiresAdmin
    @RpcDoc("Delete a radio channel.")
    suspend fun deleteChannel(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID
    ): Boolean

    @RequiresAdmin
    @RpcDoc("Set the channel's cover image from raw bytes.")
    suspend fun setChannelImage(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The raw bytes of the image.") bytes: ByteArray
    )

    @RequiresAdmin
    @RpcDoc("Add a song, artist or album to a channel's configured content.")
    suspend fun addChannelItem(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The kind of referenced entity.") type: RadioChannelItemType,
        @RpcParamDoc("The referenced entity unique identifier.") itemId: PlatformUUID
    ): Boolean

    @RequiresAdmin
    @RpcDoc("Remove a song, artist or album from a channel's configured content.")
    suspend fun removeChannelItem(
        @RpcParamDoc("The channel unique identifier.") id: PlatformUUID,
        @RpcParamDoc("The kind of referenced entity.") type: RadioChannelItemType,
        @RpcParamDoc("The referenced entity unique identifier.") itemId: PlatformUUID
    ): Boolean
}
