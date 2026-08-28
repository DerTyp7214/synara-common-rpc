@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Configuration for backing up server-side listens to a remote listen-backup receiver.")
data class ListenBackupConfig(
    @FieldDoc("Whether the backup worker pushes listens to the receiver.")
    val enabled: Boolean = false,
    @FieldDoc("Base URL of the listen-backup receiver, e.g. https://backup.example.com:8082.")
    val url: String = "",
    @FieldDoc("API key sent to the receiver. Null on update keeps the stored key; the server never returns it.")
    val key: String? = null,
    @FieldDoc("Maximum number of listens sent per request (1..10000).")
    val batchSize: Int = 1000,
)

@Serializable
@ModelDoc("Current configuration and progress of the listen backup.")
data class ListenBackupState(
    @FieldDoc("The stored configuration, with the key omitted.")
    val config: ListenBackupConfig,
    @FieldDoc("Whether an API key is stored.")
    val hasKey: Boolean,
    @FieldDoc("Identifier of this server as known to the receiver.")
    val serverId: PlatformUUID,
    @FieldDoc("Unix millis of the last completed sync run, or null if never synced.")
    val lastSyncAt: Long?,
    @FieldDoc("Update timestamp (unix millis) of the newest listen confirmed by the receiver.")
    val lastSyncedUpdatedAt: Long,
    @FieldDoc("Number of listens sent during the last sync run.")
    val lastSyncedCount: Int,
    @FieldDoc("Error message of the last failed sync run, or null if it succeeded.")
    val lastError: String?,
    @FieldDoc("Number of local listens that have not been pushed yet.")
    val pendingCount: Long,
)

@Serializable
@ModelDoc("Result of probing a listen-backup receiver.")
data class ListenBackupConnectionTest(
    @FieldDoc("Whether the receiver responded successfully.")
    val ok: Boolean,
    @FieldDoc("Error message if the probe failed.")
    val message: String? = null,
    @FieldDoc("Number of listens the receiver holds for this server, when available.")
    val remoteListenCount: Long? = null,
)
