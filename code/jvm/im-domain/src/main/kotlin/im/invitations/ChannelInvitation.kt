package im.invitations

import im.channel.Channel
import im.channel.ChannelRole
import im.user.User
import im.wrappers.Identifier
import im.wrappers.toIdentifier
import java.time.LocalDateTime

data class ChannelInvitation(
    val id: Identifier,
    val channel: Channel,
    val inviter: User,
    val invitee: User,
    val status: ChannelInvitationStatus,
    val role: ChannelRole,
    val expiresAt: LocalDateTime
) {
    companion object {
        operator fun invoke(
            id: Long = 0,
            channel: Channel,
            inviter: User,
            invitee: User,
            status: ChannelInvitationStatus = ChannelInvitationStatus.PENDING,
            role: ChannelRole,
            expiresAt: LocalDateTime
        ): ChannelInvitation {
            return ChannelInvitation(
                id = id.toIdentifier(),
                channel = channel,
                inviter = inviter,
                invitee = invitee,
                status = status,
                role = role,
                expiresAt = expiresAt
            )
        }
    }

    init {
        require(channel.members.keys.contains(inviter)) { "Inviter must be a member of the channel" }
    }

    fun accept(): ChannelInvitation = copy(status = ChannelInvitationStatus.ACCEPTED)
    fun reject(): ChannelInvitation = copy(status = ChannelInvitationStatus.REJECTED)
    fun update(role: ChannelRole, expiresAt: LocalDateTime): ChannelInvitation =
        copy(role = role, expiresAt = expiresAt)
}