package im.services.channels

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.domain.wrappers.Name
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.services.Either
import im.services.users.UserError

interface ChannelService {
    /**
     * Creates a channel.
     *
     * - The channel name must be unique.
     *
     * @param name the name of the channel
     * @param isPublic whether the channel is public or private
     * @param user the user that is creating the channel
     * @return the created channel
     */
    fun createChannel(
        name: Name,
        isPublic: Boolean,
        user: User,
    ): Either<ChannelError, Channel>

    /**
     * Retrieves a channel by its identifier.
     *
     * - A user must be a member of the channel to retrieve it.
     *
     * @param channelId the unique identifier of the channel
     * @param user the user that is requesting the channel
     * @return the channel if it exists, or a [ChannelError] otherwise
     */
    fun getChannelById(
        channelId: Identifier,
        user: User,
    ): Either<ChannelError, Channel>

    /**
     * Updates a channel.
     *
     * - A user must be the owner of the channel to update it.
     *
     * @param channelId the unique identifier of the channel
     * @param name the new name of the channel
     * @param isPublic whether the channel is public or private
     * @param user the user that is updating the channel
     * @return the updated channel
     */
    fun updateChannel(
        channelId: Identifier,
        name: Name,
        isPublic: Boolean,
        user: User,
    ): Either<ChannelError, Unit>

    /**
     * Deletes a channel.
     *
     * - A user must be the owner of the channel to delete it.
     *
     * @param channelId the unique identifier of the channel
     * @param user the user that is deleting the channel
     */
    fun deleteChannel(
        channelId: Identifier,
        user: User,
    ): Either<ChannelError, Unit>

    /**
     * Joins a channel.
     *
     * - The channel must be public or the user must have an invitation to join it.
     *
     * @param channelId the unique identifier of the channel
     * @param userId the unique identifier of the user
     * @param user the user that is joining the channel
     */
    fun joinChannel(
        channelId: Identifier,
        userId: Identifier,
        user: User,
    ): Either<ChannelError, Unit>

    /**
     * Removes a member from a channel.
     *
     * - A user must be the owner of the channel or be removing themselves to have permission to remove a member.
     *
     * @param channelId the unique identifier of the channel
     * @param userId the unique identifier of the user
     * @param user the user that is removing the member
     */
    fun removeChannelMember(
        channelId: Identifier,
        userId: Identifier,
        user: User,
    ): Either<ChannelError, Unit>

    /**
     * Retrieves a list of channels.
     *
     * @param name the name to search for
     * @param pagination the pagination request
     * @param sortRequest the sort request
     * @return a [Pagination] of channels if the search is successful, or a [ChannelError] otherwise
     */
    fun getChannels(
        name: String?,
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Either<ChannelError, Pagination<Channel>>

    /**
     * Retrieves the channels a user is a member of.
     *
     * @param userId the user identifier
     * @param user the authenticated user that is requesting the channels
     * @param sortRequest the sort request
     * @return a map of channels and their roles if the search is successful, or an [UserError] otherwise
     */
    fun getUserChannels(
        userId: Identifier,
        sortRequest: SortRequest,
        user: User,
    ): Either<ChannelError, Map<Channel, ChannelRole>>
}
