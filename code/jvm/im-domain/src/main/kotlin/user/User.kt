package user

import channel.Channel
import invitations.ChannelInvitation
import sessions.Session

data class User(
    val id: Long = 1,
    val name: String,
    val password: String,
    val sessions: List<Session> = emptyList(),
    val ownedChannels: List<Channel> = emptyList(),
    val joinedChannels: List<Channel> = emptyList(),
    val sentInvitations: List<ChannelInvitation> = emptyList(),
    val receivedInvitations: List<ChannelInvitation> = emptyList()
)