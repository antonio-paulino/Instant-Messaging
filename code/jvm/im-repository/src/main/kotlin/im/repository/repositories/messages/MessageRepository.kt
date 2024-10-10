package im.repository.repositories.messages

import im.repository.pagination.Pagination
import im.repository.repositories.Repository
import im.channel.Channel
import im.messages.Message
import im.repository.pagination.PaginationRequest
import im.wrappers.Identifier

/**
 * [Repository] for [Message] entities.
 */
interface MessageRepository : Repository<Message, Identifier> {

    /**
     * Finds all messages in a channel.
     *
     * @param channel the channel
     * @return the messages in the channel
     */
    fun findByChannel(channel: Channel): List<Message>

    /**
     * Finds the messages in a channel with pagination.
     *
     * @param channel the channel
     * @param paginationRequest the pagination request
     * @return the latest messages in the channel
     */
    fun findByChannel(channel: Channel, paginationRequest: PaginationRequest): Pagination<Message>
}