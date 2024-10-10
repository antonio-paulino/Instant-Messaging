package im.repository.repositories.invitations

import im.channel.Channel
import im.repository.repositories.Repository
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.user.User
import im.wrappers.Identifier


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
    fun findByChannel(channel: Channel, status: ChannelInvitationStatus): List<ChannelInvitation>

    /**
     * Finds all received invitations for a user.
     *
     * @param user the user
     * @return the invitations for the user
     */
    fun findByInvitee(user: User): List<ChannelInvitation>
}