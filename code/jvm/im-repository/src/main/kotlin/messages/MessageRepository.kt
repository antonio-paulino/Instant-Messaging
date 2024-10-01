package messages

import Repository

interface MessageRepository : Repository<Message, Long> {
    fun findByChannelId(channelId: Long): Iterable<Message>
    fun findLatest(channelId: Long, pages: Int, pageSize: Int): Iterable<Message>
}