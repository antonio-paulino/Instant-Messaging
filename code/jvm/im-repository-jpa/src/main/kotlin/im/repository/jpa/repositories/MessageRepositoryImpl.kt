package im.repository.jpa.repositories

import im.repository.pagination.Pagination
import im.channel.Channel
import jakarta.persistence.EntityManager
import im.messages.Message
import im.repository.jpa.model.message.MessageDTO
import im.repository.jpa.repositories.jpa.MessageRepositoryJpa
import im.repository.pagination.PaginationRequest
import im.repository.repositories.messages.MessageRepository
import im.wrappers.Identifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class MessageRepositoryImpl(
    private val messageRepositoryJpa: MessageRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : MessageRepository {

    override fun findByChannel(channel: Channel): List<Message> {
        return messageRepositoryJpa.findByChannel(channel.id.value).map { it.toDomain() }
    }

    override fun findByChannel(channel: Channel, paginationRequest: PaginationRequest): Pagination<Message> {
        val res = messageRepositoryJpa.findByChannelId(channel.id.value, utils.toPageRequest(paginationRequest, "createdAt"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun save(entity: Message): Message {
        return messageRepositoryJpa.save(MessageDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Message>): List<Message> {
        return messageRepositoryJpa.saveAll(entities.map { MessageDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Identifier): Message? {
        return messageRepositoryJpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Message> {
        return messageRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Message> {
        val res = messageRepositoryJpa.findAll(utils.toPageRequest(pagination, "createdAt"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Message> {
        return messageRepositoryJpa.findAllById(ids.map { it.value }).map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        messageRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean {
        return messageRepositoryJpa.existsById(id.value)
    }

    override fun count(): Long {
        return messageRepositoryJpa.count()
    }

    override fun deleteAll() {
        messageRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<Message>) {
        messageRepositoryJpa.deleteAll(entities.map { MessageDTO.fromDomain(it) })
    }

    override fun delete(entity: Message) {
        messageRepositoryJpa.delete(MessageDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        messageRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        entityManager.flush()
        messageRepositoryJpa.flush()
    }
}