package im.repository.repositories.messages

import im.domain.channel.Channel
import im.domain.messages.Message
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.Repository

/**
 * [Repository] for [Message] entities.
 */
interface MessageRepository : Repository<Message, Identifier> {
    /**
     * Finds the messages in a channel with pagination.
     *
     * @param channel the channel
     * @param paginationRequest the pagination request
     * @return the latest messages in the channel
     */
    fun findByChannel(
        channel: Channel,
        paginationRequest: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Message>

    /**
     * Finds a message by its identifier.
     *
     * @param id the identifier of the message
     * @return the message with the given identifier, or null if not found
     */
    fun findByChannelAndId(
        channel: Channel,
        id: Identifier,
    ): Message?
}
