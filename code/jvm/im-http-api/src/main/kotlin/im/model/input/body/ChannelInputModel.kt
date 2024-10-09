package im.model.input.body

import im.wrappers.Name
import jakarta.validation.Valid

data class ChannelInputModel(
    @Valid
    val name: Name,

    @Valid
    val isPublic: Boolean,
)