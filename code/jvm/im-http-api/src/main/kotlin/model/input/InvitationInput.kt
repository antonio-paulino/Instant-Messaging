package pt.isel.daw.daw_api.model.input

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

class InvitationInput(
    @field:NotBlank
    @Valid
    val userId: Long,
    @field:NotBlank
    @Valid
    val expirationDate: String,
    @field:NotBlank
    @Valid
    val role: String
)