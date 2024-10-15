package im.repository.mem.repositories

import im.domain.channel.Channel
import im.domain.messages.Message
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.mem.model.message.MessageDTO
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.messages.MessageRepository
import java.util.concurrent.ConcurrentHashMap

class MemMessageRepositoryImpl(
    private val utils: MemRepoUtils,
) : MessageRepository {
    private val messages = ConcurrentHashMap<Long, MessageDTO>()
    private var id = 999L // Start from 1000 to avoid conflicts with messages created in tests

    override fun findByChannel(
        channel: Channel,
        paginationRequest: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Message> {
        val filteredMessages = messages.values.filter { (it.channel.id) == channel.id.value }
        val page = utils.paginate(filteredMessages, paginationRequest, sortRequest)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findByChannelAndId(
        channel: Channel,
        id: Identifier,
    ): Message? = messages.values.find { it.id == id.value && it.channel.id == channel.id.value }?.toDomain()

    override fun save(entity: Message): Message {
        val conflict = messages.values.find { it.id == entity.id.value }
        if (conflict != null) {
            messages[conflict.id] = MessageDTO.fromDomain(entity)
            return entity
        } else {
            val newId = Identifier(++id)
            val newMessage = entity.copy(id = newId)
            messages[newId.value] = MessageDTO.fromDomain(newMessage)
            return newMessage
        }
    }

    override fun saveAll(entities: Iterable<Message>): List<Message> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: Identifier): Message? = messages.values.find { it.id == id.value }?.toDomain()

    override fun findAll(): List<Message> = messages.values.map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Message> {
        val page =
            utils.paginate(
                messages.values.map { MessageDTO.fromDomain(it.toDomain()) },
                pagination,
                sortRequest,
            )
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Message> {
        val idList = ids.map { it.value }
        return messages.values.filter { it.id in idList }.map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        messages.values.find { it.id == id.value }?.let { delete(it.toDomain()) }
    }

    override fun existsById(id: Identifier): Boolean = messages.values.any { it.id == id.value }

    override fun count(): Long = messages.size.toLong()

    override fun deleteAll() {
        id = 999L
        messages.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<Message>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: Message) {
        messages.remove(entity.id.value)
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        val idList = ids.map { it.value }
        messages.values.filter { it.id in idList }.forEach { delete(it.toDomain()) }
    }

    fun deleteAllByChannel(channel: Channel) {
        messages.values.filter { it.channel.id == channel.id.value }.forEach { delete(it.toDomain()) }
    }

    fun deleteAllByAuthor(author: User) {
        messages.values.filter { it.user.id == author.id.value }.forEach { delete(it.toDomain()) }
    }

    override fun flush() {
        // no-op
    }
}
