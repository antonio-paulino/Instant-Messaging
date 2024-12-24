package im.api.model.input.query

import im.api.model.input.validators.IsBool
import im.api.model.input.validators.IsNumber
import im.repository.pagination.PaginationRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * Input model for pagination query.
 *
 * @property limit The number of items to return.
 * @property offset The number of items to skip.
 * @property sort The sort order, either "ASC" or "DESC".
 */
data class PaginationInputModel(
    @field:Min(0, message = "Offset must be greater or equal to 0")
    @field:IsNumber("Offset must be a number")
    val offset: String = "0",
    @field:Min(1, message = "limit must be greater or equal to 1")
    @field:Max(100, message = "limit must be less than or equal to 100")
    @field:IsNumber("limit must be a number")
    val limit: String = "10",
    @field:IsBool("Get count must be true or false")
    val getCount: String = "true",
) {
    fun toRequest(): PaginationRequest =
        PaginationRequest(
            offset.toLong(),
            limit.toInt(),
            getCount.toBoolean(),
        )
}
