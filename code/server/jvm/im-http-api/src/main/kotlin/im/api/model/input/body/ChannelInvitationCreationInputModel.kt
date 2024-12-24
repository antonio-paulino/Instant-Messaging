package im.api.model.input.body

import im.api.model.input.wrappers.ChannelRole
import im.api.model.input.wrappers.Identifier
import jakarta.validation.Valid
import jakarta.validation.constraints.Future
import java.time.LocalDateTime

/**
 * Input model for inviting a user to a channel.
 *
 * @property invitee The user to invite.
 * @property expiresAt The expiration date of the invitation.
 * @property role The role of the user in the channel.
 */
class ChannelInvitationCreationInputModel(
    @field:Valid
    val invitee: Identifier,
    @field:Future(message = "Invitation expiration must be in the future")
    val expiresAt: LocalDateTime,
    @field:Valid
    val role: ChannelRole,
) {
    constructor(
        invitee: String,
        expiresAt: LocalDateTime,
        role: String,
    ) : this(
        Identifier(invitee),
        expiresAt,
        ChannelRole(role),
    )
}
