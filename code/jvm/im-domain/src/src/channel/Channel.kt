package channel

import invitations.ChannelInvitation
import messages.Message
import user.User


interface Channel {
    val id: Long
    val name: String
    val isPublic: Boolean
    val owner: User
    val members: List<User>
    val messages: List<Message>
    val invitations: List<ChannelInvitation>

    fun updateChannel(name: String, isPublic: Boolean)

    fun addMember(user: User)
    fun removeMember(user: User)

    fun addMessage(message: Message)
    fun removeMessage(message: Message)

    fun addInvitation(invitation: ChannelInvitation)
}