package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
enum class RecommendationWindow { DAY, WEEK, MONTH }

@Serializable
@ModelDoc("A mood cluster label with the number of songs assigned to it.")
data class MoodSummary(
    @FieldDoc("The mood label.")
    val mood: String,
    @FieldDoc("Number of songs assigned to this mood.")
    val count: Int
)
