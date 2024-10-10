package im.repository.pagination

/**
 * Represents a pagination request.
 *
 * Page indexing starts at 1 when using this class.
 *
 * @param page the page number
 * @param size the page size
 * @param sort the sorting order
 */
data class PaginationRequest(
    val page: Int,
    val size: Int,
    val sort: Sort = Sort.ASC
)
