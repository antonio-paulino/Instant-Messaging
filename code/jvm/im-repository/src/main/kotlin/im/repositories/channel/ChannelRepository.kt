package im.repositories.channel

import im.repositories.Repository
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.messages.Message
import im.pagination.Pagination
import im.pagination.PaginationRequest
import im.user.User
import im.wrappers.Name

/**
 * [Repository] for [Channel] entities.
 */
interface ChannelRepository : Repository<Channel, Long> {

    /**
     * Finds a list of channels by name.
     *
     * @param name the name of the channel
     * @return the channel with the given name, or `null` if no such channel exists
     */
    fun findByName(name: Name, filterPublic: Boolean): Channel?

    /**
     * Finds channels where the name starts with the given string, case-insensitive.
     *
     * @param name the partial name of the channels
     * @return the channels whose name contains the given string
     */
    fun findByPartialName(name: Name, filterPublic: Boolean, pagination: PaginationRequest): Pagination<Channel>


    /**
     * Finds all channels with pagination and filtering by public channels.
     *
     * @param pagination the pagination request
     * @param filterPublic whether to filter public channels
     * @return the channels
     */
    fun find(pagination: PaginationRequest, filterPublic: Boolean): Pagination<Channel>

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