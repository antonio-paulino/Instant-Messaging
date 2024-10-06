package im.model.input.query

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.RequestParam
import im.model.input.validators.IsNumber

data class PaginationInputModel(
    @RequestParam("page", required = false)
    @field:Min(1, message = "Page must be greater or equal to 1")
    @field:IsNumber("Page must be a number")
    val page: String = "1",

    @RequestParam("size", required = false)
    @field:Min(1, message = "Size must be greater than 0")
    @field:Max(100, message = "Size must be less or equal to 100")
    @field:IsNumber("Size must be a number")
    val size: String = "50"
)