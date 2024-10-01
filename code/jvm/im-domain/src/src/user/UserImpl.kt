package user

import channel.Channel

data class UserImpl(
    override val id: Long,
    override val name: String,
    override val password: String,
    override val ownedChannels: List<Channel>,
    override val joinedChannels: List<Channel>
): User
