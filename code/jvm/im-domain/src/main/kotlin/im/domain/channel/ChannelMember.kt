package im.domain.channel

import im.domain.user.User

/**
 * Represents a member of a channel.
 *
 * @property user the user that is a member of the channel
 * @property role the role of the user in the channel
 */
data class ChannelMember(
    val channel: Channel,
    val user: User,
    val role: ChannelRole,
)