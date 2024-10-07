package im.pagination

/**
 * Represents pagination information.
 *
 * @param total the total number of items
 * @param currentPage the current page number
 * @param totalPages the total number of pages
 * @param nextPage the next page number or null if there is no next page
 * @param prevPage the previous page number or null if there is no previous page
 */
data class PaginationInfo(
    val total: Long,
    val currentPage: Int,
    val totalPages: Int,
    val nextPage: Int?,
    val prevPage: Int?
)
