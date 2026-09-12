package dev.dertyp.services

import dev.dertyp.data.ClientDevice
import dev.dertyp.data.ClientSetting
import dev.dertyp.data.ClientSettingScope
import dev.dertyp.data.ClientSettingWrite
import dev.dertyp.data.ClientSettingsChange
import dev.dertyp.data.ClientSettingsChanges
import dev.dertyp.data.ClientSettingsSnapshot
import dev.dertyp.data.ClientSettingsWriteResult
import dev.dertyp.rpc.annotations.RestGet
import dev.dertyp.rpc.annotations.RpcDoc
import dev.dertyp.rpc.annotations.RpcParamDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
@RpcDoc(
    "Stores the settings of the clients of a user as opaque key/value entries, where the value is an arbitrary JSON document in text form and " +
        "the server never interprets what a setting means. Entries live either in the synced scope, which is shared by all devices of the user, " +
        "or in the device scope of a single device, and the client decides per key which one it uses. Each scope carries a monotonically increasing " +
        "version that every accepted write advances by one, and every entry remembers the version it was written at, so clients detect concurrent " +
        "changes and can catch up incrementally instead of reading everything again."
)
interface IClientSettingsService {
    @RestGet
    @RpcDoc("Read the entries of a scope. Tombstones of deleted keys are left out unless they are asked for explicitly. Reading the device scope registers the device or refreshes its last seen time.")
    suspend fun getSettings(
        @RpcParamDoc("The scope to read.") scope: ClientSettingScope,
        @RpcParamDoc("The device to read in the device scope. Required for the device scope and ignored in the synced scope.") device: String? = null,
        @RpcParamDoc("Whether the tombstones of deleted keys are returned as well.") includeDeleted: Boolean = false
    ): List<ClientSetting>

    @RestGet
    @RpcDoc(
        "Read the complete settings of a device in one call: the live entries of the synced scope and of the device scope with the current version of each. " +
            "Reading a snapshot registers the device or refreshes its last seen time, so this is what a client calls right after it starts."
    )
    suspend fun getSnapshot(
        @RpcParamDoc("The stable identifier the client chose for this device.") deviceId: String
    ): ClientSettingsSnapshot

    @RestGet
    @RpcDoc(
        "Read the entries a scope changed after a version the client already knows, tombstones included and ordered by the version they were written at. " +
            "When more entries are available than the page holds, the client asks again with the version of the last entry it received. " +
            "If tombstones the client never saw were already purged, the result asks for a full resync and the client re-reads the whole scope instead."
    )
    suspend fun getChanges(
        @RpcParamDoc("The scope to read.") scope: ClientSettingScope,
        @RpcParamDoc("The version the client is already up to date with. Entries written at a later version are returned.") sinceVersion: Long,
        @RpcParamDoc("The device to read in the device scope. Required for the device scope and ignored in the synced scope.") device: String? = null,
        @RpcParamDoc("Maximum number of entries to return.") limit: Int = 500
    ): ClientSettingsChanges

    @RpcDoc(
        "Write a batch of entries into a scope. The batch is all-or-nothing: an entry is accepted only if its base version matches the version the stored " +
            "entry was last written at, or is 0 for a key that does not exist or is only a tombstone, and as soon as one key conflicts nothing is written at all. " +
            "Forcing the write skips that check. An entry with a null value deletes its key and leaves a tombstone behind so other devices learn about the deletion. " +
            "Writing the device scope registers the device or refreshes its last seen time, while the device of a synced write is optional and only recorded as the writer.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun setSettings(
        @RpcParamDoc("The entries to write, each with the version the client based its change on.") entries: List<ClientSettingWrite>,
        @RpcParamDoc("The scope to write to.") scope: ClientSettingScope,
        @RpcParamDoc("The writing device. Required for the device scope, and in the synced scope only recorded as the writer of the entries.") device: String? = null,
        @RpcParamDoc("Whether to apply the entries even if the server moved past the base versions supplied.") force: Boolean = false
    ): ClientSettingsWriteResult

    @RestGet
    @RpcDoc("Read the previous values of a single key, newest first. The server keeps a bounded number of superseded values per key, so older values are dropped as new ones are written.")
    suspend fun getHistory(
        @RpcParamDoc("The scope the key belongs to.") scope: ClientSettingScope,
        @RpcParamDoc("The key to read the previous values of.") key: String,
        @RpcParamDoc("The device to read in the device scope. Required for the device scope and ignored in the synced scope.") device: String? = null,
        @RpcParamDoc("Maximum number of previous values to return.") limit: Int = 20
    ): List<ClientSetting>

    @RpcDoc(
        "Write a previous value of a key back as a new version. The value is taken from the history of the key and stored through the normal write path, " +
            "so the scope advances by one and the value that was replaced moves into the history itself. The call fails if the requested version is not kept " +
            "in the history any more.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun restore(
        @RpcParamDoc("The scope the key belongs to.") scope: ClientSettingScope,
        @RpcParamDoc("The key to restore.") key: String,
        @RpcParamDoc("The version of the historic value to write back.") version: Long,
        @RpcParamDoc("The device to write in the device scope. Required for the device scope and in the synced scope only recorded as the writer.") device: String? = null,
        @RpcParamDoc("Whether to apply the value even if the server moved past the version the entry currently has.") force: Boolean = false
    ): ClientSettingsWriteResult

    @RpcDoc(
        "Watch the settings of the user for changes. Every accepted write emits the scope it changed, the version it advanced to and the keys it touched, " +
            "including writes made by other devices, so a client pulls only what it needs."
    )
    fun observeSettings(): Flow<ClientSettingsChange>

    @RestGet
    @RpcDoc("List the devices of the user that stored settings on the server, each with the current version of its device scope.")
    suspend fun getDevices(): List<ClientDevice>

    @RpcDoc(
        "Register a device or refresh the name and platform it is listed under. Calling this is optional, since reading or writing the device scope registers " +
            "the device on its own, but it lets a client give its device a readable name before it stores anything.",
        errors = ["IllegalArgumentException"]
    )
    suspend fun registerDevice(
        @RpcParamDoc("The stable identifier the client chose for this device.") deviceId: String,
        @RpcParamDoc("Human readable name of this device.") name: String = "",
        @RpcParamDoc("Platform of this device, for example the operating system it runs on.") platform: String = ""
    ): ClientDevice

    @RpcDoc("Forget a device together with its device-scoped settings and their history. The synced scope is not touched, so the other devices of the user keep their shared settings.")
    suspend fun deleteDevice(
        @RpcParamDoc("The identifier of the device to forget.") deviceId: String
    )
}
