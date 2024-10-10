package im.repository.jpa.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationInfo
import im.repository.pagination.PaginationRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

/**
 * Utility class for Mapping JPA entities to domain objects.
 */
@Component
class JpaRepositoryUtils {

    /**
     * Converts a Spring [Page] object to a [PaginationInfo] object.
     */
    fun <T> getPaginationInfo(page: Page<T>): PaginationInfo {
        val currentPage = page.pageable.pageNumber + 1
        val nextPage = if (page.hasNext()) currentPage + 1 else null
        val prevPage = if (page.hasPrevious()) currentPage - 1 else null
        return PaginationInfo(
            page.totalElements,
            currentPage,
            page.totalPages,
            nextPage,
            prevPage
        )
    }

    /**
     * Converts a [PaginationRequest] object to a Spring [PageRequest] object.
     */
    fun toPageRequest(request: PaginationRequest, property: String): PageRequest {
        return PageRequest.of(request.page - 1, request.size, toSpringSort(request.sort), property)
    }

    /**
     * Calculates the pagination information based on the total number of elements and the pagination request.
     */
    fun <T> calculatePagination(res: List<T>, total: Long, pagination: PaginationRequest): Pagination<T> {
        val remainder = if (total % pagination.size == 0L) 0 else 1
        val totalPages = (total / pagination.size).toInt() + remainder
        val currentPage = pagination.page
        val nextPage = if (currentPage + 1 < totalPages) currentPage + 1 else null
        val prevPage = if (currentPage > 1) currentPage - 1 else null

        return Pagination(
            res,
            PaginationInfo(
                total,
                totalPages,
                currentPage,
                nextPage,
                prevPage
            )
        )
    }

    /**
     * Converts a [im.repository.pagination.Sort] object to a Spring [Sort.Direction] object.
     */
    private fun toSpringSort(sort: im.repository.pagination.Sort): Sort.Direction {
        return when (sort) {
            im.repository.pagination.Sort.ASC -> Sort.Direction.ASC
            im.repository.pagination.Sort.DESC -> Sort.Direction.DESC
        }
    }

}