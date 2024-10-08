package im.repositories

import im.pagination.Pagination
import im.pagination.PaginationInfo
import im.pagination.PaginationRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class JpaRepositoryUtils {

    fun <T> getPaginationInfo(page: Page<T>): PaginationInfo {
        val currentPage = page.pageable.pageNumber + 1
        val nextPage = if (page.hasNext()) currentPage + 1 else null
        val prevPage = if (page.hasPrevious()) currentPage - 1 else null
        return PaginationInfo(page.totalElements, currentPage, page.totalPages, nextPage, prevPage)
    }

    fun toPageRequest(request: PaginationRequest, property: String): PageRequest {
        return PageRequest.of(request.page - 1, request.size, toSpringSort(request.sort), property)
    }

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

    private fun toSpringSort(sort: im.pagination.Sort): Sort.Direction {
        return when (sort) {
            im.pagination.Sort.ASC -> Sort.Direction.ASC
            im.pagination.Sort.DESC -> Sort.Direction.DESC
        }
    }

}