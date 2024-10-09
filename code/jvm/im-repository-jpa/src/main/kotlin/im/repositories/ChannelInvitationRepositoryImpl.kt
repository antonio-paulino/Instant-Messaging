package im.repositories

import im.pagination.Pagination
import im.invitations.ChannelInvitation
import im.repositories.invitations.ChannelInvitationRepository
import im.model.invitation.ChannelInvitationDTO
import im.pagination.PaginationRequest
import im.repositories.jpa.ChannelInvitationRepositoryJpa
import im.wrappers.Identifier
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component

@Component
class ChannelInvitationRepositoryImpl(
    private val channelInvitationRepositoryJpa: ChannelInvitationRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : ChannelInvitationRepository {

    override fun save(entity: ChannelInvitation): ChannelInvitation {
        return channelInvitationRepositoryJpa.save(ChannelInvitationDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<ChannelInvitation>): List<ChannelInvitation> {
        return channelInvitationRepositoryJpa.saveAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
            .map { it.toDomain() }
    }

    override fun findById(id: Identifier): ChannelInvitation? {
        return channelInvitationRepositoryJpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<ChannelInvitation> {
        return channelInvitationRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<ChannelInvitation> {
        val res = channelInvitationRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun deleteById(id: Identifier) {
        channelInvitationRepositoryJpa.deleteById(id.value)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<ChannelInvitation> {
        return channelInvitationRepositoryJpa.findAllById(ids.map { it.value }).map { it.toDomain() }
    }

    override fun existsById(id: Identifier): Boolean {
        return channelInvitationRepositoryJpa.existsById(id.value)
    }

    override fun count(): Long {
        return channelInvitationRepositoryJpa.count()
    }

    override fun deleteAll() {
        channelInvitationRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<ChannelInvitation>) {
        channelInvitationRepositoryJpa.deleteAll(entities.map { ChannelInvitationDTO.fromDomain(it) })
    }

    override fun delete(entity: ChannelInvitation) {
        channelInvitationRepositoryJpa.delete(ChannelInvitationDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        channelInvitationRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        channelInvitationRepositoryJpa.flush()
        entityManager.flush()
    }
}