package im.repositories

import im.pagination.Pagination
import im.model.session.SessionDTO
import im.model.token.AccessTokenDTO
import im.model.token.RefreshTokenDTO
import im.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import im.sessions.Session
import im.repositories.sessions.SessionRepository
import im.tokens.AccessToken
import im.tokens.RefreshToken

@Repository
interface SessionRepositoryJpa : JpaRepository<SessionDTO, Long>

@Component
class SessionRepositoryImpl(
    private val sessionRepositoryJpa: SessionRepositoryJpa,
    private val entityManager: EntityManager
) : SessionRepository {

    override fun getAccessTokens(session: Session): List<AccessToken> {
        val query = entityManager.createQuery(
            "SELECT a FROM AccessTokenDTO a WHERE a.session.id = :sessionId",
            AccessTokenDTO::class.java
        )
        query.setParameter("sessionId", session.id)
        return query.resultList.map { it.toDomain() }
    }

    override fun getRefreshTokens(session: Session): List<RefreshToken> {
        val query = entityManager.createQuery(
            "SELECT r FROM RefreshTokenDTO r WHERE r.session.id = :sessionId",
            RefreshTokenDTO::class.java
        )
        query.setParameter("sessionId", session.id)
        return query.resultList.map { it.toDomain() }
    }

    override fun save(entity: Session): Session {
        return sessionRepositoryJpa.save(SessionDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Session>): List<Session> {
        return sessionRepositoryJpa.saveAll(entities.map { SessionDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Session? {
        return sessionRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Session> {
        return sessionRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pair<List<Session>, Pagination> {
        val res = sessionRepositoryJpa.findAll(pagination.toPageRequest("id"))
        return res.content.map { it.toDomain() } to res.getPagination(res.pageable)
    }

    override fun findAllById(ids: Iterable<Long>): List<Session> {
        return sessionRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        sessionRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return sessionRepositoryJpa.existsById(id)
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

    override fun deleteAllById(ids: Iterable<Long>) {
        sessionRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        sessionRepositoryJpa.flush()
    }
}