package im.repository.jpa.repositories


import im.repository.jpa.model.session.SessionDTO
import im.repository.jpa.repositories.jpa.SessionRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import im.sessions.Session
import im.repository.repositories.sessions.SessionRepository
import im.user.User
import im.wrappers.Identifier
import org.springframework.context.annotation.Primary

@Component
@Primary
class SessionRepositoryImpl(
    private val sessionRepositoryJpa: SessionRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : SessionRepository {

    override fun findByUser(user: User): List<Session> {
        return sessionRepositoryJpa.findByUser(user.id.value).map { it.toDomain() }
    }

    override fun save(entity: Session): Session {
        return sessionRepositoryJpa.save(SessionDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Session>): List<Session> {
        return sessionRepositoryJpa.saveAll(entities.map { SessionDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Identifier): Session? {
        return sessionRepositoryJpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Session> {
        return sessionRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Session> {
        val res = sessionRepositoryJpa.findAll(utils.toPageRequest(pagination, "id"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Session> {
        return sessionRepositoryJpa.findAllById(ids.map { it.value }).map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        sessionRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean {
        return sessionRepositoryJpa.existsById(id.value)
    }

    override fun count(): Long {
        return sessionRepositoryJpa.count()
    }

    override fun deleteAll() {
        sessionRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<Session>) {
        sessionRepositoryJpa.deleteAll(entities.map { SessionDTO.fromDomain(it) })
    }

    override fun delete(entity: Session) {
        sessionRepositoryJpa.delete(SessionDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        sessionRepositoryJpa.deleteAllById(ids.map { it.value })
    }

    override fun flush() {
        entityManager.flush()
        sessionRepositoryJpa.flush()
    }
}