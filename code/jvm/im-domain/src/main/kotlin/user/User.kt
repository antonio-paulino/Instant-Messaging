package user

import channel.Channel
import channel.ChannelMember
import invitations.ChannelInvitation
import sessions.Session

data class User(
    val id: Long = 1,
    val name: String,
    val password: String,
    val sessions: List<Session> = emptyList(),
    val joinedChannels: List<ChannelMember> = emptyList(),
    val ownedChannels: List<Channel> = emptyList(),
    val sentInvitations: List<ChannelInvitation> = emptyList(),
    val receivedInvitations: List<ChannelInvitation> = emptyList()
)