package dev.dertyp.data

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("A generic wrapper for paginated data collections.")
data class PaginatedResponse<T>(
    @FieldDoc("The collection of items for the current page.")
    val data: List<T>,
    @FieldDoc("The current page index (starting from 0).")
    val page: Int = 0,
    @FieldDoc("The total number of items across all pages.")
    val total: Int = 0,
    @FieldDoc("The number of items per page.")
    val pageSize: Int = 150,
    @FieldDoc("Whether there are more pages available.")
    val hasNextPage: Boolean = false,
)
