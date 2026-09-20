package dev.dertyp.data

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The catalog an entry of the release feed originates from.")
enum class ReleaseSource {
    MusicBrainz,
    Apple
}
