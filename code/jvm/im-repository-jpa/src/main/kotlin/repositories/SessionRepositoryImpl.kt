package repositories

import jakarta.persistence.EntityManager
import model.session.SessionDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import sessions.Session
import sessions.SessionRepository
import java.util.*

@Repository
interface SessionRepositoryJpa : JpaRepository<SessionDTO, Long>

@Component
class SessionRepositoryImpl : SessionRepository {

    @Autowired
    private lateinit var sessionRepositoryJpa: SessionRepositoryJpa

    @Autowired
    private lateinit var entityManager: EntityManager

    override fun save(entity: Session): Session {
        return sessionRepositoryJpa.save(SessionDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<Session>): List<Session> {
        return sessionRepositoryJpa.saveAll(entities.map { SessionDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: Long): Optional<Session> {
        return sessionRepositoryJpa.findById(id).map { it.toDomain() }
    }

    override fun findAll(): Iterable<Session> {
        return sessionRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun findFirst(page: Int, pageSize: Int): List<Session> {
        val res = sessionRepositoryJpa.findAll(Pageable.ofSize(pageSize).withPage(page))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<Session> {
        val query = entityManager.createQuery(
            "SELECT s FROM SessionDTO s ORDER BY s.id DESC",
            SessionDTO::class.java
        )
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun findAllById(ids: Iterable<Long>): Iterable<Session> {
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
}