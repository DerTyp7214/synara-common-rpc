package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Determines whether a settings entry is shared by all devices of the user or belongs to a single device. The client picks the scope per key.")
enum class ClientSettingScope {
    @FieldDoc("The entry is shared by every device of the user and follows the user to new installations.") SYNCED,

    @FieldDoc("The entry belongs to one device only and is addressed together with its device identifier.") DEVICE
}

@Serializable
@ModelDoc(
    "A single stored settings entry. The server is agnostic to what a setting means: the key is an opaque identifier chosen by the client and " +
        "the value is an arbitrary JSON document stored as text."
)
data class ClientSetting(
    @FieldDoc("The opaque key of the entry, unique within its scope.")
    val key: String,
    @FieldDoc("The stored value as a JSON document in text form, or null when the entry is a tombstone.")
    val value: String? = null,
    @FieldDoc("Whether the entry is a tombstone, meaning the key was deleted and is only kept so other devices learn about the deletion.")
    val deleted: Boolean = false,
    @FieldDoc("The scope version this entry was last written at. The client passes it back as the base version of its next write to the key.")
    val version: Long,
    @FieldDoc("Unix timestamp in milliseconds of the write that produced this entry.")
    val modifiedAt: Long,
    @FieldDoc("The device that performed the write, or null if the writer did not identify itself.")
    val modifiedByDeviceId: String? = null,
    @FieldDoc("The scope the entry belongs to.")
    val scope: ClientSettingScope,
    @FieldDoc("The device the entry belongs to in the device scope, and null in the synced scope.")
    val deviceId: String? = null
)

@Serializable
@ModelDoc("A single change to a settings key, either a new value or a deletion, carrying the version the client based the change on.")
data class ClientSettingWrite(
    @FieldDoc("The opaque key to write.")
    val key: String,
    @FieldDoc("The value to store as a JSON document in text form. A null value deletes the key and leaves a tombstone behind.")
    val value: String? = null,
    @FieldDoc(
        "The version of the entry the client last saw, or 0 if it believes the key does not exist. The write is rejected with a conflict " +
            "when the stored entry moved past this version, unless the write is forced."
    )
    val baseVersion: Long = 0
)

@Serializable
@ModelDoc("A single key of a rejected write whose stored entry moved past the base version the client supplied.")
data class ClientSettingConflict(
    @FieldDoc("The key that was not written.")
    val key: String,
    @FieldDoc("The base version the client supplied for this key.")
    val baseVersion: Long,
    @FieldDoc("The entry currently stored on the server, or null if the key does not exist at all.")
    val current: ClientSetting? = null
)

@Serializable
@ModelDoc("Result of a settings write, either the applied entries or the keys that are in conflict. A write is all-or-nothing.")
sealed class ClientSettingsWriteResult {
    @FieldDoc("The version of the scope after the write, or the unchanged current version when the write was rejected.")
    abstract val version: Long

    @Serializable
    @SerialName("Ok")
    @ModelDoc("Every entry of the batch was applied and the scope advanced to a new version.")
    data class Ok(
        @FieldDoc("The version of the scope after the write.")
        override val version: Long,
        @FieldDoc("The entries as they are now stored, each stamped with the new version.")
        val entries: List<ClientSetting>
    ) : ClientSettingsWriteResult()

    @Serializable
    @SerialName("Conflict")
    @ModelDoc(
        "Nothing was written because the listed keys moved past the base version the client supplied. The client pulls those keys and retries " +
            "with the version it now knows, or repeats the write with force enabled to overwrite the server state."
    )
    data class Conflict(
        @FieldDoc("The unchanged current version of the scope.")
        override val version: Long,
        @FieldDoc("The keys of the batch that were in conflict, each with the entry currently stored on the server.")
        val conflicts: List<ClientSettingConflict>
    ) : ClientSettingsWriteResult()
}

@Serializable
@ModelDoc(
    "The complete settings of one device: the live entries of the synced and the device scope with the current version of each. " +
        "Reading a snapshot registers the device or refreshes its last seen time."
)
data class ClientSettingsSnapshot(
    @FieldDoc("The device the snapshot was taken for.")
    val deviceId: String,
    @FieldDoc("The current version of the synced scope.")
    val syncedVersion: Long,
    @FieldDoc("The current version of the device scope of this device.")
    val deviceVersion: Long,
    @FieldDoc(
        "The live entries of both scopes, each tagged with the scope it belongs to and without tombstones. A device entry overrides a synced " +
            "entry of the same key on the client."
    )
    val entries: List<ClientSetting>
)

@Serializable
@ModelDoc("A page of entries a scope changed since a version the client already knows, used to catch up without reading the whole scope.")
data class ClientSettingsChanges(
    @FieldDoc("The scope the changes belong to.")
    val scope: ClientSettingScope,
    @FieldDoc("The device the changes belong to in the device scope, and null in the synced scope.")
    val deviceId: String? = null,
    @FieldDoc("The current version of the scope.")
    val version: Long,
    @FieldDoc("The entries written after the requested version, tombstones included, ordered by the version they were written at.")
    val entries: List<ClientSetting>,
    @FieldDoc("Whether more entries follow. The client asks again with the version of the last entry it received.")
    val hasMore: Boolean = false,
    @FieldDoc(
        "Whether tombstones the client never saw were already purged, so the changes are incomplete. The client has to read the whole scope " +
            "again instead of applying them."
    )
    val fullResync: Boolean = false
)

@Serializable
@ModelDoc("Notification that a scope advanced to a new version, so other devices can pull the keys that changed.")
data class ClientSettingsChange(
    @FieldDoc("The scope that changed.")
    val scope: ClientSettingScope,
    @FieldDoc("The device whose scope changed in the device scope, and null in the synced scope.")
    val deviceId: String? = null,
    @FieldDoc("The version the scope advanced to.")
    val version: Long,
    @FieldDoc("The keys written by this change, deletions included.")
    val keys: List<String>,
    @FieldDoc("The device that performed the write, or null if the writer did not identify itself.")
    val modifiedByDeviceId: String? = null
)

@Serializable
@ModelDoc("A device of the user that stores settings on the server. Devices are identified by a stable identifier the client generates and persists itself.")
data class ClientDevice(
    @FieldDoc("The stable identifier the client chose for itself.")
    val deviceId: String,
    @FieldDoc("Human readable name the device reported for itself.")
    val name: String = "",
    @FieldDoc("Platform the device reported for itself, for example the operating system it runs on.")
    val platform: String = "",
    @FieldDoc("Unix timestamp in milliseconds at which the device was first seen.")
    val createdAt: Long,
    @FieldDoc("Unix timestamp in milliseconds of the last time the device read or wrote its settings.")
    val lastSeenAt: Long,
    @FieldDoc("The current version of the device scope of this device, or 0 if it never stored a device setting.")
    val settingsVersion: Long = 0
)
