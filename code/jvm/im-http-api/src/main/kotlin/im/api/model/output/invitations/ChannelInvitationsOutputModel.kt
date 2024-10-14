package im.api.model.output.invitations

import im.domain.invitations.ChannelInvitation

data class ChannelInvitationsOutputModel(
    val invitations: List<ChannelInvitationOutputModel>,
) {
    companion object {
        fun fromDomain(invitations: List<ChannelInvitation>): ChannelInvitationsOutputModel =
            ChannelInvitationsOutputModel(
                invitations = invitations.map { ChannelInvitationOutputModel.fromDomain(it) },
            )
    }
}
