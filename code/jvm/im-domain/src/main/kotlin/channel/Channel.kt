package channel

import user.User
import java.time.LocalDateTime

data class Channel(
    val id: Long = 0,
    val name: String,
    val owner: User,
    val isPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val members: Map<User, ChannelRole> = hashMapOf(owner to ChannelRole.OWNER),
) {
    fun updateChannel(name: String, isPublic: Boolean) = copy(name = name, isPublic = isPublic)
    fun addMember(user: User, role: ChannelRole) = copy(members = members + (user to role))
    fun removeMember(user: User) = copy(members = members - user)
}