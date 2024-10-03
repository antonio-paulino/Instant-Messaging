package messages

import Repository
import channel.Channel

/**
 * [Repository] for [Message] entities.
 */
interface MessageRepository : Repository<Message, Long> {

    /**
     * Finds all messages in a channel.
     *
     * @param channel the channel
     * @return the messages in the channel
     */
    fun findByChannel(channel: Channel): List<Message>

    /**
     * Finds the latest messages in a channel.
     *
     * @param channel the channel
     * @param pages the number of pages to skip
     * @param pageSize the number of messages to return
     * @return the latest messages in the channel
     */
    fun findLatest(channel: Channel, pages: Int, pageSize: Int): List<Message>
}