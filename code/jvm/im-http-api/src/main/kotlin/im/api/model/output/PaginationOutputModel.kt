package im.api.model.output

import im.repository.pagination.PaginationInfo

data class PaginationOutputModel(
    val total: Long?,
    val totalPages: Int?,
    val current: Int,
    val next: Int?,
    val previous: Int?,
) {
    companion object {
        fun fromPagination(pagination: PaginationInfo?): PaginationOutputModel? {
            if (pagination == null) {
                return null
            }
            return PaginationOutputModel(
                total = pagination.total,
                totalPages = pagination.totalPages,
                current = pagination.currentPage,
                next = pagination.nextPage,
                previous = pagination.prevPage,
            )
        }
    }
}
