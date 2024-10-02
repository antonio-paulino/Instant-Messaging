package channel

import invitations.ChannelRole
import user.User

data class ChannelMember(
    val channel: Channel,
    val user: User,
    val role: ChannelRole
)