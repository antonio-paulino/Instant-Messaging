package im.repository.mem.repositories

import im.channel.Channel
import im.messages.Message
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.messages.MessageRepository
import im.repository.mem.model.message.MessageDTO
import im.user.User
import im.wrappers.Identifier
import java.util.concurrent.ConcurrentHashMap

class MemMessageRepositoryImpl(
    private val utils: MemRepoUtils
) : MessageRepository {

    private val messages = ConcurrentHashMap<Long, MessageDTO>()
    private var id = 999L // Start from 1000 to avoid conflicts with messages created in tests

    override fun findByChannel(channel: Channel): List<Message> {
        return messages.values.filter { (it.channel.id) == channel.id.value }.map { it.toDomain() }
    }

    override fun findByChannel(channel: Channel, paginationRequest: PaginationRequest): Pagination<Message> {
        val filteredMessages = messages.values.filter { (it.channel.id) == channel.id.value }
        val page = utils.paginate(filteredMessages, paginationRequest, "createdAt")
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun save(entity: Message): Message {
        val conflict = messages.values.find { it.id == entity.id.value }
        if (conflict != null) {
            messages[entity.id.value] = MessageDTO.fromDomain(entity)
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

    override fun findById(id: Identifier): Message? {
        return messages[id.value]?.toDomain()
    }

    override fun findAll(): List<Message> {
        return messages.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Message> {
        val page = utils.paginate(messages.values.toList(), pagination)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Message> {
        return messages.values.filter { it.id in ids.map { id -> id.value } }.map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        if (messages.containsKey(id.value)) {
            delete(messages[id.value]!!.toDomain())
        }
    }

    override fun existsById(id: Identifier): Boolean {
        return messages.containsKey(id.value)
    }

    override fun count(): Long {
        return messages.size.toLong()
    }

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
        ids.forEach { deleteById(it) }
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