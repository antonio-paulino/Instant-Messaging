package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort

class MemRepoUtils {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> paginate(
        items: List<T>,
        pagination: PaginationRequest,
        sortBy: String = "id"
    ): Pagination<T> {

        if (items.isEmpty()) {
            return Pagination(
                emptyList(),
                PaginationInfo(0, 1, 1, null, null)
            )
        }

        val sortedChannels = items.sortedBy { channel ->
            val sortField = T::class.java.getDeclaredField(sortBy)
            sortField.isAccessible = true
            sortField.get(channel) as Comparable<Any>
        }

        val total = items.size
        val currentPage = pagination.page
        val totalPages = ((total + pagination.size - 1) / pagination.size)
        val nextPage = if (pagination.page < totalPages) pagination.page + 1 else null
        val prevPage = if (pagination.page > 1) pagination.page - 1 else null

        val fromIndex = (pagination.page - 1) * pagination.size
        val toIndex = (fromIndex + pagination.size).coerceAtMost(total)

        val paginatedChannels = if (pagination.sort == Sort.ASC) {
            sortedChannels.subList(fromIndex, toIndex)
        } else {
            sortedChannels.reversed().subList(fromIndex, toIndex)
        }

        return Pagination(
            paginatedChannels,
            PaginationInfo(total.toLong(), currentPage, totalPages, nextPage, prevPage)
        )
    }

}