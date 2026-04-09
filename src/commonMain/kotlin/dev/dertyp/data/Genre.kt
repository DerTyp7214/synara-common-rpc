@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.data

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Represents a musical genre or category.")
data class Genre(
    @FieldDoc("The genre unique identifier.")
    val id: PlatformUUID,
    @FieldDoc("The name of the genre.")
    val name: String
)
