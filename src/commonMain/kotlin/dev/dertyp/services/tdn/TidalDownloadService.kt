package dev.dertyp.services.tdn

import kotlinx.serialization.Serializable

@Serializable
enum class TidalDownloadService {
    Tdn,
    Tiddl
}