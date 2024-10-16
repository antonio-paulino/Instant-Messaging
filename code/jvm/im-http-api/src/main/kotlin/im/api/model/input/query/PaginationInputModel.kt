package im.api.model.input.query

import im.api.model.input.validators.IsBool
import im.api.model.input.validators.IsNumber
import im.repository.pagination.PaginationRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * Input model for pagination query.
 *
 * @property page The page number.
 * @property size The number of items per page.
 * @property sort The sort order, either "ASC" or "DESC".
 */
data class PaginationInputModel(
    @field:Min(1, message = "Page must be greater or equal to 1")
    @field:IsNumber("Page must be a number")
    val page: String = "1",
    @field:Min(1, message = "Size must be greater than 0")
    @field:Max(100, message = "Size must be less or equal to 100")
    @field:IsNumber("Size must be a number")
    val size: String = "50",
    @field:IsBool("Get count must be true or false")
    val getCount: String = "true",
) {
    fun toRequest(): PaginationRequest =
        PaginationRequest(
            page = page.toInt(),
            size = size.toInt(),
            getCount = getCount.toBoolean(),
        )
}
