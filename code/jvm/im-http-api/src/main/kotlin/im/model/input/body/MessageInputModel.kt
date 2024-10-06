package im.model.input.body

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

// TODO: Add validations
class MessageInputModel(
    @field:NotBlank
    @Valid
    val content: String
)