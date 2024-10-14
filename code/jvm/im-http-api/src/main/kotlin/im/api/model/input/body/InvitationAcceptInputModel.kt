package im.api.model.input.body

import im.api.model.input.validators.ChannelInvitationStatus

data class InvitationAcceptInputModel(
    @field:ChannelInvitationStatus
    val status: String,
)
