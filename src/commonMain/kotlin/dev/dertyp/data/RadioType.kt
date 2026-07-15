package dev.dertyp.data

import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("The source strategy used to seed a radio station.")
enum class RadioType { RANDOM, LAST_WEEK, LAST_MONTH, LAST_YEAR }
