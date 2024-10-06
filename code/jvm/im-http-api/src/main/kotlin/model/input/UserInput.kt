package pt.isel.daw.daw_api.model.input

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserInput(
    @field:NotBlank
    @Valid
    val name: String,
    @field:NotBlank
    @Email
    val email: String,
    @field:NotBlank
    @Valid
    val password: String
)