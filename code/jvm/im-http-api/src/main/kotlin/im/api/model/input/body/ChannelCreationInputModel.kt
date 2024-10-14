package im.api.model.input.body

import im.api.model.input.validators.IsBool
import im.api.model.input.wrappers.Name
import jakarta.validation.Valid

/**
 * Input model for creating a channel.
 *
 * @property name The name of the channel.
 * @property isPublic Whether the channel is public.
 */
data class ChannelCreationInputModel(
    @field:Valid
    val name: Name,
    @field:IsBool
    val isPublic: String,
) {
    constructor(
        name: String,
        isPublic: Boolean,
    ) : this(
        Name(name),
        isPublic.toString(),
    )
}
