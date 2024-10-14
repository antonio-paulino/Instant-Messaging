package im.api.model.output.invitations

import im.domain.invitations.ChannelInvitation

data class ChannelInvitationCreationOutputModel(
    val id: Long,
) {
    companion object {
        fun fromDomain(invitation: ChannelInvitation): ChannelInvitationCreationOutputModel =
            ChannelInvitationCreationOutputModel(
                id = invitation.id.value,
            )
    }
}
