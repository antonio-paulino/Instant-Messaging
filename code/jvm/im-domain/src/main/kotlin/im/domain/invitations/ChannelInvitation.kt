package im.domain.invitations

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import java.time.LocalDateTime

/**
 * Represents an invitation to a channel.
 *
 * @property id The unique identifier of the invitation.
 * @property channel The channel to which the user is invited.
 * @property inviter The user that sent the invitation.
 * @property invitee The user that received the invitation.
 * @property status The status of the invitation.
 * @property role The role that the user will have in the channel.
 * @property expiresAt The date and time when the invitation expires.
 */
data class ChannelInvitation(
    val id: Identifier,
    val channel: Channel,
    val inviter: User,
    val invitee: User,
    val status: ChannelInvitationStatus,
    val role: ChannelRole,
    val expiresAt: LocalDateTime,
) {
    constructor(
        id: Long = 0,
        channel: Channel,
        inviter: User,
        invitee: User,
        status: ChannelInvitationStatus = ChannelInvitationStatus.PENDING,
        role: ChannelRole,
        expiresAt: LocalDateTime,
    ) : this(
        id = Identifier(id),
        channel = channel,
        inviter = inviter,
        invitee = invitee,
        status = status,
        role = role,
        expiresAt = expiresAt,
    )

    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    val isValid: Boolean
        get() = !expired && status == ChannelInvitationStatus.PENDING

    init {
        require(channel.members.keys.contains(inviter)) { "Inviter must be a member of the channel" }
    }

    /**
     * Accepts the invitation.
     *
     * @return a new invitation with the status set to accepted
     */
    fun accept(): ChannelInvitation = copy(status = ChannelInvitationStatus.ACCEPTED)

    /**
     * Rejects the invitation.
     *
     * @return a new invitation with the status set to rejected
     */
    fun reject(): ChannelInvitation = copy(status = ChannelInvitationStatus.REJECTED)

    /**
     * Updates the role and expiration date of the invitation.
     */
    fun update(
        role: ChannelRole,
        expiresAt: LocalDateTime,
    ): ChannelInvitation = copy(role = role, expiresAt = expiresAt)
}
