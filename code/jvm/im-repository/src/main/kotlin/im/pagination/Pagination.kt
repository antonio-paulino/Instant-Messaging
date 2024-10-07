package im.pagination

/**
 * Represents a pagination response.
 * @param total the total number of items
 * @param currentPage the current page
 * @param totalPages the total number of pages
 * @param nextPage the next page number, or null if there is no next page
 * @param prevPage the previous page number, or null if there is no previous page
 */
data class Pagination(
    val total: Long,
    val currentPage: Int,
    val totalPages: Int,
    val nextPage: Int?,
    val prevPage: Int?,
)