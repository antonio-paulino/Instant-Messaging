package pt.isel.daw.daw_api.model.input

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.web.bind.annotation.RequestParam

data class PaginationInput(
    @RequestParam("page",required = false)
    @field:Min(1, message = "Page must be greater or equal to 1")
    @Valid
    val page: Int = 1,
    @RequestParam("size",required = false)
    @field:Min(1, message = "Size must be greater than 0")
    @Valid
    val size: Int = 50
)