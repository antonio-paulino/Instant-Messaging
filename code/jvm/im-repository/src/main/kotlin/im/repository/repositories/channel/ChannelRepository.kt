package im.repository.repositories.channel

import im.repository.repositories.Repository
import im.channel.Channel
import im.channel.ChannelRole
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.user.User
import im.wrappers.Identifier
import im.wrappers.Name

/**
 * [Repository] for [Channel] entities.
 */
interface ChannelRepository : Repository<Channel, Identifier> {

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
    fun findByPartialName(name: String, filterPublic: Boolean, pagination: PaginationRequest): Pagination<Channel>


    /**
     * Finds all channels with pagination and filtering by public channels.
     *
     * @param pagination the pagination request
     * @param filterPublic whether to filter public channels
     * @return the channels
     */
    fun find(pagination: PaginationRequest, filterPublic: Boolean): Pagination<Channel>


    /**
     * Finds the role of a user in a channel.
     *
     * @param channel the channel
     * @param user the user
     * @return the role of the user in the channel, or `null` if the user is not a member
     */
    fun getMember(channel: Channel, user: User): Pair<User, ChannelRole>?

    /**
     * Finds all owned channels for a user.
     *
     * @param user the user
     * @return the channels for the user
     */
    fun findByOwner(user: User): List<Channel>

    /**
     * Finds all channels for a user.
     *
     * @param user the user
     * @return the channels for the user
     */
    fun findByMember(user: User): Map<Channel, ChannelRole>
}