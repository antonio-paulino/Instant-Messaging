package im.api.model.output.invitations

import im.api.model.output.channel.ChannelOutputModel
import im.api.model.output.users.UserOutputModel
import java.time.LocalDateTime

data class ChannelInvitationOutputModel(
    val id: Long,
    val channel: ChannelOutputModel,
    val inviter: UserOutputModel,
    val invitee: UserOutputModel,
    val status: String,
    val role: String,
    val expiresAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(invitation: im.domain.invitations.ChannelInvitation): ChannelInvitationOutputModel =
            ChannelInvitationOutputModel(
                id = invitation.id.value,
                channel = ChannelOutputModel.fromDomain(invitation.channel),
                inviter = UserOutputModel.fromDomain(invitation.inviter),
                invitee = UserOutputModel.fromDomain(invitation.invitee),
                status = invitation.status.toString(),
                role = invitation.role.toString(),
                invitation.expiresAt,
            )
    }
}
