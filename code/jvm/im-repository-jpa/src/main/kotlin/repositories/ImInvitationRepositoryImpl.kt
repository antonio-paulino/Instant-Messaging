package repositories

import invitations.ImInvitation
import invitations.ImInvitationRepository
import jakarta.persistence.EntityManager
import model.invitation.ImInvitationDTO
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ImInvitationRepositoryJpa : JpaRepository<ImInvitationDTO, UUID>

@Component
class ImInvitationRepositoryImpl(
    private val imInvitationRepositoryJpa: ImInvitationRepositoryJpa,
    private val entityManager: EntityManager
) : ImInvitationRepository {

    override fun findByToken(token: UUID): ImInvitation? {
        val query = entityManager.createQuery(
            "SELECT i FROM ImInvitationDTO i WHERE i.token = :token",
            ImInvitationDTO::class.java
        )
        query.setParameter("token", token)
        return query.resultList.firstOrNull()?.toDomain()
    }

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

    override fun findFirst(page: Int, pageSize: Int): List<ImInvitation> {
        val pageable = org.springframework.data.domain.PageRequest.of(page, pageSize, Sort.by("expiresAt"))
        val res = imInvitationRepositoryJpa.findAll(pageable)
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<ImInvitation> {
        val query = entityManager.createQuery(
            "SELECT i FROM ImInvitationDTO i ORDER BY i.expiresAt DESC",
            ImInvitationDTO::class.java
        )
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
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