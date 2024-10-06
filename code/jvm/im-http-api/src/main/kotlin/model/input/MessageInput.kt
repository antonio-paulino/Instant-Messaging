package pt.isel.daw.daw_api.model.input

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

class MessageInput(
    @field:NotBlank
    @Valid
    val content: String
)