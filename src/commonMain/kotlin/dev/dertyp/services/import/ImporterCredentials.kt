package dev.dertyp.services.import

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Base type for credentials supplied to an importer backend.")
sealed class ImporterCredentials

@Serializable
@ModelDoc("Apple Music credentials for the gamdl importer.")
data class GamdlCredentials(
    val cookiesTxt: String,
    val wvdBase64: String? = null,
) : ImporterCredentials()
