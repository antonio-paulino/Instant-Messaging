package im.repository.jpa.repositories

import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
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
