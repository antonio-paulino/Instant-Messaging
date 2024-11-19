import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * A pageable implementation that uses an offset and limit to determine the page.
 *
 * @param offset the number of items to skip
 * @param limit the page size
 */
data class OffsetPageable(
    private val offset: Long,
    private val limit: Int,
    private val sort: Sort,
) : Pageable {
    override fun getPageNumber(): Int = (offset / limit).toInt()

    override fun getPageSize(): Int = limit

    override fun getOffset(): Long = offset

    override fun getSort(): Sort = sort

    override fun next(): Pageable = OffsetPageable(offset + limit, limit, sort)

    override fun previousOrFirst(): Pageable =
        if (hasPrevious()) {
            OffsetPageable(offset - limit, limit, sort)
        } else {
            first()
        }

    override fun first(): Pageable = OffsetPageable(0, limit, sort)

    override fun withPage(pageNumber: Int): Pageable = OffsetPageable((pageNumber * limit).toLong(), limit, sort)

    override fun hasPrevious(): Boolean = offset > 0
}
