package im.model.input.body

import jakarta.validation.constraints.NotBlank

// TODO: Add all the necessary validations
data class ChannelInputModel(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val isPublic: Boolean
)