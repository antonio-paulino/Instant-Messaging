package channel

import invitations.ChannelInvitation
import invitations.ChannelRole
import messages.Message
import user.User
import java.time.LocalDateTime


data class Channel(
    val id: Long = 0,
    val name: String,
    val owner: User,
    val isPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val members: List<User> = emptyList(),
    val messages: List<Message> = emptyList(),
    val invitations: List<ChannelInvitation> = emptyList()
) {
    fun updateChannel(name: String, isPublic: Boolean) = copy(name = name, isPublic = isPublic)
    fun addMessage(message: Message) = copy(messages = messages + message)
    fun removeMessage(message: Message) = copy(messages = messages - message)
    fun addInvitation(invitation: ChannelInvitation) = copy(invitations = invitations + invitation)
    fun addMember(user: User, role: ChannelRole) = copy(members = members + user)
    fun removeMember(user: User) = copy(members = members - user)
}