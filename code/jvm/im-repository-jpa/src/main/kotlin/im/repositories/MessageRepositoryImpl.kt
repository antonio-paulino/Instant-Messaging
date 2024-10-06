package im.repositories

import im.channel.Channel
import jakarta.persistence.EntityManager
import im.messages.Message
import im.messages.MessageRepository
import im.model.message.MessageDTO
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface MessageRepositoryJpa : JpaRepository<MessageDTO, Long>

@Component
class MessageRepositoryImpl(
    private val messageRepositoryJpa: MessageRepositoryJpa,
    private val entityManager: EntityManager
) : MessageRepository {

    override fun findByChannel(channel: Channel): List<Message> {
        val query = entityManager.createQuery(
            "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId",
            MessageDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        return query.resultList.map { it.toDomain() }
    }

    override fun findLatest(channel: Channel, pages: Int, pageSize: Int): List<Message> {
        val query = entityManager.createQuery(
            "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId ORDER BY m.createdAt DESC",
            MessageDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        query.firstResult = pages * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun save(entity: Message): Message {
        return messageRepositoryJpa.save(MessageDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Message>): List<Message> {
        return messageRepositoryJpa.saveAll(entities.map { MessageDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Message? {
        return messageRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Message> {
        return messageRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun findFirst(page: Int, pageSize: Int): List<Message> {
        val res = messageRepositoryJpa.findAll(PageRequest.of(page, pageSize))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<Message> {
        val query = entityManager.createQuery("SELECT m FROM MessageDTO m ORDER BY m.id DESC", MessageDTO::class.java)
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun findAllById(ids: Iterable<Long>): List<Message> {
        return messageRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        messageRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return messageRepositoryJpa.existsById(id)
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

    override fun deleteAllById(ids: Iterable<Long>) {
        messageRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        messageRepositoryJpa.flush()
    }
}