package dev.dertyp.services.tdn

import kotlinx.serialization.Serializable

@Suppress("EnumEntryName")
enum class TdnFavoriteType {
    tracks,
    artists,
    albums,
    videos
}

@Serializable
data class ProcessExecutionResult(val exitCode: Int, val fullOutput: String, val error: String) {
    companion object {
        val EMPTY = ProcessExecutionResult(-2, "", "")
    }

    fun successful(): Boolean = exitCode == 0
    fun failed(): Boolean = exitCode == 1
    fun unknown(): Boolean = exitCode == -2
}