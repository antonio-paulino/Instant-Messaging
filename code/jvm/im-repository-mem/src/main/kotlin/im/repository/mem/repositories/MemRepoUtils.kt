package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest

class MemRepoUtils {
    inline fun <reified T> paginate(
        items: List<T>,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<T> {
        if (items.isEmpty()) {
            return Pagination(
                emptyList(),
                PaginationInfo(0, 1, 0, null, null),
            )
        }

        val sortedChannels = handleSort(items, sortRequest)

        val total = items.size
        val currentPage = (pagination.offset / pagination.limit + 1).toInt()
        val totalPages = (total + pagination.limit - 1) / pagination.limit
        val nextPage = if (totalPages > currentPage) currentPage + 1 else null
        val prevPage = if (currentPage > 1) currentPage - 1 else null

        val startIndex = pagination.offset.coerceAtMost(sortedChannels.size.toLong())
        val endIndex = (pagination.offset + pagination.limit).coerceAtMost(sortedChannels.size.toLong())

        val paginatedChannels = sortedChannels.subList(startIndex.toInt(), endIndex.toInt())

        val info =
            PaginationInfo(
                total.toLong(),
                currentPage,
                totalPages,
                nextPage,
                prevPage,
            )

        return if (pagination.getCount) {
            Pagination(paginatedChannels, info)
        } else {
            Pagination(paginatedChannels, info.copy(total = null, totalPages = null))
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> handleSort(
        items: List<T>,
        sortRequest: SortRequest,
    ): List<T> {
        val sort = sortRequest.sortBy ?: "id"
        val sortField = T::class.java.getDeclaredField(sort).apply { isAccessible = true }
        return if (sortRequest.direction == Sort.ASC) {
            items.sortedBy { sortField.get(it) as Comparable<Any> }
        } else {
            items.sortedByDescending { sortField.get(it) as Comparable<Any> }
        }
    }
}
