package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import java.awt.SystemColor.info

class MemRepoUtils {
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> paginate(
        items: List<T>,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
        getCount: Boolean = true,
    ): Pagination<T> {
        if (items.isEmpty()) {
            return Pagination(
                emptyList(),
                PaginationInfo(0, 1, 0, null, null),
            )
        }

        val sortedChannels = handleSort(items, sortRequest)

        val total = items.size
        val currentPage = pagination.page
        val totalPages = if (total == 0) 0 else ((total + pagination.size - 1) / pagination.size)
        val nextPage = if (pagination.page < totalPages) pagination.page + 1 else null
        val prevPage = if (pagination.page > 1) pagination.page - 1 else null

        val fromIndex = (pagination.page - 1) * pagination.size
        val toIndex = (fromIndex + pagination.size).coerceAtMost(total)

        val paginatedChannels = sortedChannels.subList(fromIndex, toIndex)

        val info =
            PaginationInfo(
                total.toLong(),
                currentPage,
                totalPages,
                nextPage,
                prevPage,
            )

        return if (getCount) {
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
