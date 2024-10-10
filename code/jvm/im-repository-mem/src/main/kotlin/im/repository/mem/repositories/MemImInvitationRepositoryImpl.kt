package im.repository.mem.repositories

import im.invitations.ImInvitation
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.invitations.ImInvitationRepository
import im.repository.mem.model.invitation.ImInvitationDTO
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MemImInvitationRepositoryImpl(
    private val utils: MemRepoUtils
) : ImInvitationRepository {

    private val invitations = ConcurrentHashMap<UUID, ImInvitationDTO>()

    override fun save(entity: ImInvitation): ImInvitation {
        val conflict = invitations.values.find { it.token == entity.token }
        if (conflict != null) {
            if (conflict.status == entity.status) {
                throw IllegalStateException("Cannot use invitation twice")
            }
            invitations[entity.token] = ImInvitationDTO.fromDomain(entity)
            return entity
        } else {
            invitations[entity.token] = ImInvitationDTO.fromDomain(entity)
            return entity
        }
    }

    override fun saveAll(entities: Iterable<ImInvitation>): List<ImInvitation> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: UUID): ImInvitation? {
        return invitations[id]?.toDomain()
    }

    override fun findAll(): List<ImInvitation> {
        return invitations.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<ImInvitation> {
        val page = utils.paginate(invitations.values.toList(), pagination, "expiresAt")
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<UUID>): List<ImInvitation> {
        return invitations.values.filter { it.token in ids }.map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        if (invitations.containsKey(id)) {
            delete(invitations[id]!!.toDomain())
        }
    }

    override fun existsById(id: UUID): Boolean {
        return invitations.containsKey(id)
    }

    override fun count(): Long {
        return invitations.size.toLong()
    }

    override fun deleteAll() {
        invitations.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<ImInvitation>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: ImInvitation) {
        invitations.remove(entity.token)
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        ids.forEach { deleteById(it) }
    }

    override fun flush() {
        // No-op
    }


}