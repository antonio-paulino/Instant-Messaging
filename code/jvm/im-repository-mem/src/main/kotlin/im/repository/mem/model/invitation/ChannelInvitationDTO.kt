package im.repository.mem.model.invitation

import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.channel.ChannelRole
import im.repository.mem.model.channel.ChannelDTO
import im.repository.mem.model.user.UserDTO
import im.wrappers.toIdentifier
import java.time.LocalDateTime

data class ChannelInvitationDTO(
    val id: Long = 0,
    val channel: ChannelDTO,
    val inviter: UserDTO,
    val invitee: UserDTO,
    val status: ChannelInvitationStatus = ChannelInvitationStatus.PENDING,
    val role: ChannelRole = ChannelRole.MEMBER,
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
) {
    companion object {
        fun fromDomain(invitation: ChannelInvitation): ChannelInvitationDTO =
            ChannelInvitationDTO(
                id = invitation.id.value,
                channel = ChannelDTO.fromDomain(invitation.channel),
                inviter = UserDTO.fromDomain(invitation.inviter),
                invitee = UserDTO.fromDomain(invitation.invitee),
                status = invitation.status,
                role = invitation.role,
                expiresAt = invitation.expiresAt
            )
    }

    fun toDomain(): ChannelInvitation = ChannelInvitation(
        id = id.toIdentifier(),
        channel = channel.toDomain(),
        inviter = inviter.toDomain(),
        invitee = invitee.toDomain(),
        status = status,
        role = role,
        expiresAt = expiresAt
    )
}