package dev.dertyp.services.download

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Available backend services for downloading content.")
data class DownloadBackend(val id: String) {
    companion object {
        val Tdn = DownloadBackend("tdn")
        val Tiddl = DownloadBackend("tiddl")
        val Youtube = DownloadBackend("youtube")
    }
}
