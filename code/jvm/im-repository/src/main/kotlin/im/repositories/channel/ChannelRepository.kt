package im.repositories.channel

import im.repositories.Repository
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.messages.Message
import im.user.User

/**
 * [Repository] for [Channel] entities.
 */
interface ChannelRepository : Repository<Channel, Long> {
    /**
     * Finds a channel by its name.
     *
     * @param name the name of the channel
     * @return the channel with the given name, or `null` if no such channel exists
     */
    fun findByName(name: String): Channel?

    /**
     * Finds channels where the name starts with the given string, case-insensitive.
     *
     * @param name the partial name of the channels
     * @return the channels whose name contains the given string
     */
    fun findByPartialName(name: String): List<Channel>

    /**
     * Finds all invitations to a channel with a given status.
     *
     * @param channel the channel
     * @param status the status of the invitations
     * @return the invitations to the channel with the given status
     */
    fun getInvitations(channel: Channel, status: ChannelInvitationStatus): List<ChannelInvitation>

    /**
     * Finds all messages in a channel.
     *
     * @param channel the channel
     * @return the messages in the channel
     */
    fun getMessages(channel: Channel): List<Message>

    /**
     * Finds the role of a user in a channel.
     *
     * @param channel the channel
     * @param user the user
     * @return the role of the user in the channel, or `null` if the user is not a member
     */
    fun getMember(channel: Channel, user: User): Pair<User, ChannelRole>?
}