package im.repository.jpa.repositories

import im.domain.sessions.Session
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.jpa.model.session.SessionDTO
import im.repository.jpa.repositories.jpa.SessionRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.sessions.SessionRepository
import jakarta.persistence.EntityManager
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class SessionRepositoryImpl(
    private val sessionRepositoryJpa: SessionRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils,
) : SessionRepository {
    override fun findByUser(user: User): List<Session> = sessionRepositoryJpa.findByUserId(user.id.value).map { it.toDomain() }

    override fun deleteExpired() {
        sessionRepositoryJpa.deleteAllByExpiresAtIsBefore()
    }

    override fun save(entity: Session): Session = sessionRepositoryJpa.save(SessionDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<Session>): List<Session> =
        sessionRepositoryJpa
            .saveAll(
                entities.map {
                    SessionDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: Identifier): Session? =
        sessionRepositoryJpa
            .findById(id.value)
            .map {
                it.toDomain()
            }.orElse(null)

    override fun findAll(): List<Session> = sessionRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Session> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                sessionRepositoryJpa.findAll(pageable)
            } else {
                sessionRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Session> =
        sessionRepositoryJpa
            .findAllById(
                ids.map {
                    it.value
                },
            ).map { it.toDomain() }

    override fun deleteById(id: Identifier) {
        sessionRepositoryJpa.deleteById(id.value)
    }

    override fun existsById(id: Identifier): Boolean = sessionRepositoryJpa.existsById(id.value)

    override fun count(): Long = sessionRepositoryJpa.count()

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
        sessionRepositoryJpa.flush()
    }
}
