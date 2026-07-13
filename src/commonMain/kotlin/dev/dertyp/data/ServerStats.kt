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
    @FieldDoc("Total number of animated images stored.")
    val animatedImagesCount: Int,
    @FieldDoc("Total number of system playlists.")
    val playlistCount: Int,
    @FieldDoc("Total file size of the media library in bytes.")
    val totalFileSize: Long,
    @FieldDoc("Size of files that have been successfully indexed.")
    val indexedFileSize: Long,
    @FieldDoc("Total disk space consumed by stored images in bytes.")
    val imagesFileSize: Long = 0L,
    @FieldDoc("Total disk space consumed by stored animated images in bytes.")
    val animatedImagesFileSize: Long = 0L,
    @FieldDoc("Average file size per track in bytes.")
    val averageSizePerSong: Long,
    @FieldDoc("Summed duration of all tracks in milliseconds.")
    val totalDuration: Long,
    @FieldDoc("Statistics about transcoded audio versions.")
    val transcodeStats: List<TranscodeStats>,
    @FieldDoc("Statistics about the MusicBrainz metadata cache.")
    val musicBrainzCache: MusicBrainzCacheStats,
    @FieldDoc("Information about the server software version.")
    val version: Version
) {
    @Serializable
    @ModelDoc("Statistics for a specific bitrate and format combination.")
    data class TranscodeStats(
        @FieldDoc("The audio bitrate in kbps.")
        val bitrate: Int,
        @FieldDoc("The audio format.")
        val format: AudioFormat,
        @FieldDoc("Number of songs transcoded to this version.")
        val count: Int,
        @FieldDoc("Total file size in bytes.")
        val totalSize: Long
    )

    @Serializable
    @ModelDoc("Statistics about the MusicBrainz metadata cache.")
    data class MusicBrainzCacheStats(
        @FieldDoc("Total number of cached artists.")
        val artistCount: Int,
        @FieldDoc("Number of stale artists in the cache.")
        val staleArtistCount: Int,
        @FieldDoc("Total number of cached release groups.")
        val releaseGroupCount: Int,
        @FieldDoc("Number of stale release groups in the cache.")
        val staleReleaseGroupCount: Int,
        @FieldDoc("Total number of cached releases.")
        val releaseCount: Int,
        @FieldDoc("Number of stale releases in the cache.")
        val staleReleaseCount: Int,
        @FieldDoc("Total number of cached recordings.")
        val recordingCount: Int,
        @FieldDoc("Number of stale recordings in the cache.")
        val staleRecordingCount: Int,
    )

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
