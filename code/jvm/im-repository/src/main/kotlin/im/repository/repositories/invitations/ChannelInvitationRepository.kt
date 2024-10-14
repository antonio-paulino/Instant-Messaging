package im.repository.repositories.invitations

import im.domain.channel.Channel
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.pagination.SortRequest
import im.repository.repositories.Repository

/**
 * [Repository] for [ChannelInvitation] entities.
 */
interface ChannelInvitationRepository : Repository<ChannelInvitation, Identifier> {
    /**
     * Finds all invitations for a channel.
     *
     * @param channel the channel
     * @return the invitations for the channel
     */
    fun findByChannel(
        channel: Channel,
        status: ChannelInvitationStatus,
        sortRequest: SortRequest,
    ): List<ChannelInvitation>

    /**
     * Finds all received invitations for a user.
     *
     * @param user the user
     * @return the invitations for the user
     */
    fun findByInvitee(
        user: User,
        sortRequest: SortRequest,
    ): List<ChannelInvitation>

    /**
     * Finds an invitation for a user and a channel.
     *
     * @param user the user
     * @param channel the channel
     * @return the invitation for the user and channel
     */
    fun findByInviteeAndChannel(
        user: User,
        channel: Channel,
    ): ChannelInvitation?

    /**
     * Deletes all expired invitations.
     */
    fun deleteExpired()
}
