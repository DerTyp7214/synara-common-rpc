package dev.dertyp.services.import

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("Available backend services for importing content.")
data class ImportBackend(val id: String)
