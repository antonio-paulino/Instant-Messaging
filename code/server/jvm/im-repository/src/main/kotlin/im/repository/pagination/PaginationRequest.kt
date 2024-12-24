package im.repository.pagination

/**
 * Represents a pagination request.
 *
 * @param offset the offset of the first element to return
 * @param limit the maximum number of elements to return
 * @param getCount whether to count the total number of elements
 */
data class PaginationRequest(
    val offset: Long,
    val limit: Int,
    val getCount: Boolean = true,
)
