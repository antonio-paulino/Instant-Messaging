package im.repositories

import im.pagination.Pagination
import im.invitations.ImInvitation
import im.repositories.invitations.ImInvitationRepository
import im.model.invitation.ImInvitationDTO
import im.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ImInvitationRepositoryJpa : JpaRepository<ImInvitationDTO, UUID>

@Component
class ImInvitationRepositoryImpl(
    private val imInvitationRepositoryJpa: ImInvitationRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : ImInvitationRepository {

    override fun save(entity: ImInvitation): ImInvitation {
        return imInvitationRepositoryJpa.save(ImInvitationDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<ImInvitation>): List<ImInvitation> {
        return imInvitationRepositoryJpa.saveAll(entities.map { ImInvitationDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: UUID): ImInvitation? {
        return imInvitationRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<ImInvitation> {
        return imInvitationRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<ImInvitation> {
        val res = imInvitationRepositoryJpa.findAll(utils.toPageRequest(pagination, "expiresAt"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<ImInvitation> {
        return imInvitationRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        imInvitationRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean {
        return imInvitationRepositoryJpa.existsById(id)
    }

    override fun count(): Long {
        return imInvitationRepositoryJpa.count()
    }

    override fun deleteAll() {
        imInvitationRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<ImInvitation>) {
        imInvitationRepositoryJpa.deleteAll(entities.map { ImInvitationDTO.fromDomain(it) })
    }

    override fun delete(entity: ImInvitation) {
        imInvitationRepositoryJpa.delete(ImInvitationDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        imInvitationRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        imInvitationRepositoryJpa.flush()
    }

}