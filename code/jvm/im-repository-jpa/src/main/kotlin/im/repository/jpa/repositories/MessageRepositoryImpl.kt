package im.repository.jpa.repositories

import im.domain.channel.Channel
import im.domain.messages.Message
import im.domain.wrappers.identifier.Identifier
import im.repository.jpa.model.message.MessageDTO
import im.repository.jpa.repositories.jpa.MessageRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.messages.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class MessageRepositoryImpl(
    private val messageRepositoryJpa: MessageRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : MessageRepository {
    override fun findByChannel(
        channel: Channel,
        paginationRequest: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Message> {
        val pagination = utils.toPageRequest(paginationRequest, sortRequest)
        val res =
            if (paginationRequest.getCount) {
                messageRepositoryJpa.findByChannelId(channel.id.value, pagination)
            } else {
                messageRepositoryJpa.findByChannelIdSliced(channel.id.value, pagination)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findByChannelAndId(
        channel: Channel,
        id: Identifier,
    ): Message? = messageRepositoryJpa.findByChannelIdAndId(channel.id.value, id.value)?.toDomain()

    override fun save(entity: Message): Message = messageRepositoryJpa.save(MessageDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<Message>): List<Message> =
        messageRepositoryJpa
            .saveAll(
                entities.map {
                    MessageDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: Identifier): Message? =
        messageRepositoryJpa
            .findById(id.value)
            .map {
                it.toDomain()
            }.orElse(null)

    override fun findAll(): List<Message> = messageRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Message> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                messageRepositoryJpa.findAll(pageable)
            } else {
                messageRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Message> =
        messageRepositoryJpa
            .findAllById(
                ids.map {
                    it.value
                },
            ).map { it.toDomain() }

    override fun deleteById(id: Identifier) {
        messageRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean = messageRepositoryJpa.existsById(id.value)

    override fun count(): Long = messageRepositoryJpa.count()

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
        messageRepositoryJpa.flush()
    }
}
