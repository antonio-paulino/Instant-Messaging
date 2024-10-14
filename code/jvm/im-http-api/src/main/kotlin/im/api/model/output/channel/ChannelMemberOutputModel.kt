package im.api.model.output.channel

import im.domain.channel.ChannelRole
import im.domain.user.User

data class ChannelMemberOutputModel(
    val id: Long,
    val name: String,
    val role: String,
) {
    companion object {
        fun fromDomain(
            user: User,
            role: ChannelRole,
        ) = ChannelMemberOutputModel(
            id = user.id.value,
            name = user.name.value,
            role = role.name,
        )
    }
}
