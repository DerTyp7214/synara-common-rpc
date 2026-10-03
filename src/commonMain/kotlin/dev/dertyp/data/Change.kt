package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@ModelDoc("The kind of state a change notification is about. Each topic names the getter a client calls again to read the new state.")
enum class ChangeTopic {
    @FieldDoc("A topic this client does not know yet. Clients ignore it.")
    UNKNOWN,

    @FieldDoc("The connected devices of the user changed. Read them again with @IClientRequestService.getOnlineDevices.")
    ONLINE_DEVICES,

    @FieldDoc("The pinned home cards or their layout changed. Read them again with @IUiService.getHomeCards.")
    HOME_CARDS,

    @FieldDoc("The listen history or the song playing right now changed. Read it again with @IScrobbleService.recentListens, @IScrobbleService.recentArtists or @IScrobbleService.recentAlbums.")
    LISTENS,

    @FieldDoc("The ListenBrainz link or its sync status changed. Read it again with @IListenBrainzService.getStatus.")
    LISTENBRAINZ_STATUS
}

object ChangeTopicSerializer : KSerializer<ChangeTopic> {
    override val descriptor: SerialDescriptor = ChangeTopic.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ChangeTopic) {
        ChangeTopic.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): ChangeTopic {
        val name = decoder.decodeString()
        return ChangeTopic.entries.firstOrNull { it.name == name } ?: ChangeTopic.UNKNOWN
    }
}

@Serializable
@ModelDoc("A notification that a piece of the user's state changed. It carries no data, so the client reads the new state with the getter the @ChangeTopic names.")
data class Change(
    @FieldDoc("What changed. Topics a client does not know are decoded as UNKNOWN.")
    @Serializable(with = ChangeTopicSerializer::class)
    val topic: ChangeTopic
)
