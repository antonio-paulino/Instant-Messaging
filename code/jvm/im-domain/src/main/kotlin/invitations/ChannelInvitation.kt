package invitations

import channel.Channel
import user.User
import java.time.LocalDateTime

data class ChannelInvitation(
    val id: Long,
    val channel: Channel,
    val inviter: User,
    val invitee: User,
    val status: ChannelInvitationStatus,
    val role: ChannelRole,
    val expiresAt: LocalDateTime
) {
    fun accept(): ChannelInvitation = copy(status = ChannelInvitationStatus.ACCEPTED)
    fun reject(): ChannelInvitation = copy(status = ChannelInvitationStatus.REJECTED)
    fun update(role: ChannelRole, expiresAt: LocalDateTime): ChannelInvitation =
        copy(role = role, expiresAt = expiresAt)
}