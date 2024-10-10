package im.model.input.wrappers

import im.channel.ChannelRole
import im.model.input.validators.Role

data class ChannelRole(
    @field:Role
    val value: String
) {
    fun toDomain() = ChannelRole.valueOf(value)
}