package pt.isel.daw.daw_api.model.input

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class ChannelInput(
    @field:NotBlank
    @Valid
    val name: String,
    @field:NotBlank
    @Valid
    val isPublic: Boolean
)