package channel

import Repository
import invitations.ChannelRole
import user.User

interface ChannelRepository: Repository<Channel, Long> {
    fun findByName(name: String): Channel?
    fun findByPartialName(name: String): Iterable<Channel>
    fun getUserRoles(channel: Channel): Map<User, ChannelRole>
    fun addMember(channel: Channel, user: User, role: ChannelRole): Channel
    fun removeMember(channel: Channel, user: User): Channel
}