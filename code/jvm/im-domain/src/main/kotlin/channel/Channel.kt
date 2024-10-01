package channel

import invitations.ChannelInvitation
import messages.Message
import user.User
import java.time.LocalDateTime


data class Channel(
    val id: Long,
    val name: String,
    val owner: User,
    val isPublic: Boolean,
    val createdAt: LocalDateTime,
    val members: List<User>,
    val messages: List<Message>,
    val invitations: List<ChannelInvitation>
) {
    fun updateChannel(name: String, isPublic: Boolean) = copy(name = name, isPublic = isPublic)
    fun addMember(user: User) = copy(members = members + user)
    fun removeMember(user: User) = copy(members = members - user)
    fun addMessage(message: Message) = copy(messages = messages + message)
    fun removeMessage(message: Message) = copy(messages = messages - message)
    fun addInvitation(invitation: ChannelInvitation) = copy(invitations = invitations + invitation)
}