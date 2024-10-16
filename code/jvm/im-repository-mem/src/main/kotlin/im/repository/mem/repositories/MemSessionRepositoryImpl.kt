package im.repository.mem.repositories

import im.domain.sessions.Session
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.mem.model.session.SessionDTO
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.sessions.SessionRepository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class MemSessionRepositoryImpl(
    private val utils: MemRepoUtils,
    private val accessTokenRepository: MemAccessTokenRepositoryImpl,
    private val refreshTokenRepository: MemRefreshTokenRepositoryImpl,
) : SessionRepository {
    private val sessions = ConcurrentHashMap<Long, SessionDTO>()
    private var id = 999L // Start from 1000 to avoid conflicts with sessions created in tests

    override fun findByUser(user: User): List<Session> = sessions.values.filter { it.user.toDomain() == user }.map { it.toDomain() }

    override fun deleteExpired() {
        sessions.values.filter { it.expiresAt.isBefore(LocalDateTime.now()) }.forEach { delete(it.toDomain()) }
    }

    override fun save(entity: Session): Session {
        val conflict = sessions.values.find { it.id == entity.id.value }
        if (conflict != null) {
            sessions[entity.id.value] = SessionDTO.fromDomain(entity)
            return entity
        } else {
            val newId = Identifier(++id)
            val newSession = entity.copy(id = newId)
            sessions[newId.value] = SessionDTO.fromDomain(newSession)
            return newSession
        }
    }

    override fun saveAll(entities: Iterable<Session>): List<Session> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: Identifier): Session? = sessions[id.value]?.toDomain()

    override fun findAll(): List<Session> = sessions.values.map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<Session> {
        val page = utils.paginate(sessions.values.toList(), pagination, sortRequest)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Session> =
        sessions.values
            .filter {
                it.id in ids.map { id -> id.value }
            }.map { it.toDomain() }

    override fun deleteById(id: Identifier) {
        if (sessions.containsKey(id.value)) {
            delete(sessions[id.value]!!.toDomain())
        }
    }

    override fun existsById(id: Identifier): Boolean = sessions.containsKey(id.value)

    override fun count(): Long = sessions.size.toLong()

    override fun deleteAll() {
        id = 999L
        sessions.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<Session>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: Session) {
        accessTokenRepository.deleteAllBySession(entity)
        refreshTokenRepository.deleteAllBySession(entity)
        sessions.remove(entity.id.value)
    }

    override fun deleteAllById(ids: Iterable<Identifier>) {
        ids.forEach { deleteById(it) }
    }

    fun deleteAllByUser(user: User) {
        sessions.values.filter { it.user.toDomain() == user }.forEach { delete(it.toDomain()) }
    }

    override fun flush() {
        // No-op in memory context
    }
}
