package im.model.output

import im.invitations.ImInvitation

data class ImInvitationOutputModel(
    val token: String,
    val status: String,
    val expiresAt: String,
) {
    companion object {
        fun fromDomain(invitation: ImInvitation): ImInvitationOutputModel {
            return ImInvitationOutputModel(
                token = invitation.token.toString(),
                status = invitation.status.name,
                expiresAt = invitation.expiresAt.toString(),
            )
        }
    }
}
