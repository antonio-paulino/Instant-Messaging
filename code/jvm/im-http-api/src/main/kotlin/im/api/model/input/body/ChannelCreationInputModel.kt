package im.api.model.input.body

import im.api.model.input.validators.IsBool
import im.api.model.input.wrappers.ChannelRole
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
    @field:Valid
    val defaultRole: ChannelRole,
    @field:IsBool
    val isPublic: String,
) {
    constructor(
        name: String,
        defaultRole: im.domain.channel.ChannelRole,
        isPublic: Boolean,
    ) : this(
        Name(name),
        ChannelRole(defaultRole.toString()),
        isPublic.toString(),
    )
}
