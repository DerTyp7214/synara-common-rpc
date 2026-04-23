package dev.dertyp.services.download

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Available backend services for downloading content.")
enum class DownloadBackend {
    Tdn,
    Tiddl,
    Youtube
}
