package pt.isel.daw.daw_api.model.output

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserOutput(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @Email
    val email: String
)

