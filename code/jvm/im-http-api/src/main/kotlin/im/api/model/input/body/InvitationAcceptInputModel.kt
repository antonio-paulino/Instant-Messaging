package im.api.model.input.body

import im.api.model.input.validators.domain.ChannelInvitationStatusValid

/**
 * Input model for accepting or rejecting an invitation.
 *
 * @property status The status of the invitation.
 */
data class InvitationAcceptInputModel(
    @field:ChannelInvitationStatusValid
    val status: String,
)
