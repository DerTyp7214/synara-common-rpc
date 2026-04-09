package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Detailed system metrics and library statistics.")
data class ServerStats(
    @FieldDoc("Total number of songs in the library.")
    val songCount: Int,
    @FieldDoc("Total number of albums.")
    val albumCount: Int,
    @FieldDoc("Total number of artists.")
    val artistCount: Int,
    @FieldDoc("Total number of images stored.")
    val imagesCount: Int,
    @FieldDoc("Total number of system playlists.")
    val playlistCount: Int,
    @FieldDoc("Total file size of the media library in bytes.")
    val totalFileSize: Long,
    @FieldDoc("Size of files that have been successfully indexed.")
    val indexedFileSize: Long,
    @FieldDoc("Average file size per track in bytes.")
    val averageSizePerSong: Long,
    @FieldDoc("Summed duration of all tracks in milliseconds.")
    val totalDuration: Long,
    @FieldDoc("Information about the server software version.")
    val version: Version
) {
    @Serializable
    @ModelDoc("Details about the server's version and build environment.")
    data class Version(
        @FieldDoc("The application version number.")
        val version: String,
        @FieldDoc("Timestamp of when the server was built.")
        val buildTime: String,
        @FieldDoc("The Git commit hash for this build.")
        val commitHash: String,
        @FieldDoc("The Java runtime version.")
        val runtime: String,
        @FieldDoc("The operating system kernel information.")
        val kernel: String,
    )
}
