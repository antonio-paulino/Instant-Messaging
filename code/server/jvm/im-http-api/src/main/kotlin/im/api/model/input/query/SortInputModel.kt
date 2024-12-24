package im.api.model.input.query

import im.api.model.input.validators.domain.IsSort
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest

data class SortInputModel(
    val sortBy: String? = null,
    @field:IsSort
    val sort: String = "ASC",
) {
    fun toRequest(): SortRequest = SortRequest(sortBy, Sort.valueOf(sort.uppercase()))
}
