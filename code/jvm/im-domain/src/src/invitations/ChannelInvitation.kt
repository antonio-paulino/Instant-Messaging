package invitations

import channel.Channel
import user.User
import java.util.*

interface ChannelInvitation {
    val id: Long
    val channel: Channel
    val inviter: User
    val invitee: User
    val status : ChannelInvitationStatus
    val role : ChannelRole
    val expirationDate : Date

    fun accept() : ChannelInvitation
    fun reject() : ChannelInvitation

    fun update(role: ChannelRole, expirationDate: Date)
}