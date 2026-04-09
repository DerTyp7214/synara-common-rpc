package dev.dertyp.services.tdn

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Available backend services for downloading content from Tidal.")
enum class TidalDownloadService {
    Tdn,
    Tiddl
}