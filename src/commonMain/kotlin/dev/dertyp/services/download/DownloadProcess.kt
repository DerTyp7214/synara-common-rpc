package dev.dertyp.services.download

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Suppress("EnumEntryName")
@ModelDoc("The category of Download favorites.")
enum class DownloadFavType {
    tracks,
    artists,
    albums,
    videos,
}

@Serializable
@ModelDoc("Results of an external process execution (e.g., downloader).")
data class ProcessExecutionResult(
    @FieldDoc("The process exit code (0 for success).")
    val exitCode: Int,
    @FieldDoc("The complete standard output of the process.")
    val fullOutput: String,
    @FieldDoc("The complete error output of the process.")
    val error: String
) {
    companion object {
        val EMPTY = ProcessExecutionResult(-2, "", "")
    }

    fun successful(): Boolean = exitCode == 0
    fun failed(): Boolean = exitCode == 1
    fun unknown(): Boolean = exitCode == -2
}