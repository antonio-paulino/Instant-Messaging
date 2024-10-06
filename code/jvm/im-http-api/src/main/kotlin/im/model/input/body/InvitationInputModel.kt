package im.model.input.body

import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import im.model.input.validators.IsNumber

// TODO: Add validations
class InvitationInputModel(
    @field:NotBlank
    @field:IsNumber
    val userId: String,
    @field:NotBlank
    @field:Future
    val expiration: String,
    @field:NotBlank
    @Valid
    val role: String
)