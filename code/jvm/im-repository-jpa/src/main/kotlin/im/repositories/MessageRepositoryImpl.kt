package im.repositories

import im.pagination.Pagination
import im.channel.Channel
import jakarta.persistence.EntityManager
import im.messages.Message
import im.repositories.messages.MessageRepository
import im.model.message.MessageDTO
import im.pagination.PaginationInfo
import im.pagination.PaginationRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface MessageRepositoryJpa : JpaRepository<MessageDTO, Long>

@Component
class MessageRepositoryImpl(
    private val messageRepositoryJpa: MessageRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : MessageRepository {

    override fun findByChannel(channel: Channel): List<Message> {
        val query = entityManager.createQuery(
            "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId",
            MessageDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        return query.resultList.map { it.toDomain() }
    }

    override fun findByChannel(channel: Channel, paginationRequest: PaginationRequest): Pagination<Message> {
        val totalMessagesQuery = entityManager.createQuery(
            "SELECT COUNT(m) FROM MessageDTO m WHERE m.channel.id = :channelId",
            Long::class.java
        )
        totalMessagesQuery.setParameter("channelId", channel.id)
        val totalMessages = totalMessagesQuery.singleResult

        val query = entityManager.createQuery(
            "SELECT m FROM MessageDTO m WHERE m.channel.id = :channelId ORDER BY m.createdAt ${paginationRequest.sort}",
            MessageDTO::class.java
        )
        query.setParameter("channelId", channel.id)
        query.firstResult = (paginationRequest.page - 1) * paginationRequest.size
        query.maxResults = paginationRequest.size
        val res = query.resultList

        val remainder = if (totalMessages % paginationRequest.size == 0L) 0 else 1
        val totalPages = (totalMessages / paginationRequest.size).toInt() + remainder
        val currentPage = paginationRequest.page
        val nextPage = if (currentPage + 1 < totalPages) currentPage + 1 else null
        val prevPage = if (currentPage > 1) currentPage - 1 else null

        val pagination = Pagination(
            res.map { it.toDomain() },
            PaginationInfo(
                totalMessages,
                totalPages,
                currentPage,
                nextPage,
                prevPage
            )
        )

        return pagination
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

    override fun find(pagination: PaginationRequest): Pagination<Message> {
        val res = messageRepositoryJpa.findAll(utils.toPageRequest(pagination, "createdAt"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
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