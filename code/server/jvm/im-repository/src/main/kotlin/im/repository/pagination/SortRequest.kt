package im.repository.pagination

data class SortRequest(
    val sortBy: String?,
    val direction: Sort = Sort.ASC,
)
