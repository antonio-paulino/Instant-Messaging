package im.repository.jpa.repositories

import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import jakarta.persistence.TypedQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

/**
 * Utility class for Mapping JPA entities to domain objects.
 */
@Component
class JpaRepositoryUtils {
    fun <T> getPaginationInfo(slice: Slice<T>): PaginationInfo {
        val currentPage = slice.pageable.pageNumber + 1
        val nextPage = if (slice.hasNext()) currentPage + 1 else null
        val prevPage = if (slice.hasPrevious()) currentPage - 1 else null
        val (total, totalPages) =
            if (slice is Page<T>) {
                slice.totalElements to slice.totalPages
            } else {
                null to null
            }
        return PaginationInfo(
            total,
            currentPage,
            totalPages,
            nextPage,
            prevPage,
        )
    }

    fun <T> handleQueryPagination(
        query: TypedQuery<T>,
        pagination: PaginationRequest,
    ): Pair<List<T>, PaginationInfo> {
        query.firstResult = (pagination.page - 1) * pagination.size
        query.maxResults = pagination.size + 1 // +1 to check if there is a next page
        val res = query.resultList
        val next = if (res.size <= pagination.size) null else pagination.page + 1
        val prev = if (pagination.page == 1) null else pagination.page - 1
        if (next != null) res.removeLast()
        return Pair(res, PaginationInfo(nextPage = next, prevPage = prev, currentPage = pagination.page))
    }

    /**
     * Converts a [PaginationRequest] object to a Spring [PageRequest] object.
     */
    fun toPageRequest(
        request: PaginationRequest,
        sort: SortRequest,
    ): PageRequest = PageRequest.of(request.page - 1, request.size, sort.direction.toSpringSortDirection(), sort.sortBy)

    fun toSort(sort: SortRequest): Sort = Sort.by(sort.direction.toSpringSortDirection(), sort.sortBy)

    /**
     * Converts a [im.repository.pagination.Sort] object to a Spring [Sort.Direction] object.
     */
    private fun im.repository.pagination.Sort.toSpringSortDirection(): Sort.Direction =
        when (this) {
            im.repository.pagination.Sort.ASC -> Sort.Direction.ASC
            im.repository.pagination.Sort.DESC -> Sort.Direction.DESC
        }
}
