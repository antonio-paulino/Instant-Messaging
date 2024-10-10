package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.sessions.SessionRepository
import im.sessions.Session
import im.user.User
import im.wrappers.Identifier
import java.util.concurrent.ConcurrentHashMap

class MemSessionRepositoryImpl(
    private val utils: MemRepoUtils,
    private val accessTokenRepository: MemAccessTokenRepositoryImpl,
    private val refreshTokenRepository: MemRefreshTokenRepositoryImpl
) : SessionRepository {

    private val sessions = ConcurrentHashMap<Long, im.repository.mem.model.session.SessionDTO>()
    private var id = 999L // Start from 1000 to avoid conflicts with sessions created in tests

    override fun findByUser(user: User): List<Session> {
        return sessions.values.filter { it.user.toDomain() == user }.map { it.toDomain() }
    }

    override fun save(entity: Session): Session {
        val conflict = sessions.values.find { it.id == entity.id.value }
        if (conflict != null) {
            sessions[entity.id.value] = im.repository.mem.model.session.SessionDTO.fromDomain(entity)
            return entity
        } else {
            val newId = Identifier(++id)
            val newSession = entity.copy(id = newId)
            sessions[newId.value] = im.repository.mem.model.session.SessionDTO.fromDomain(newSession)
            return newSession
        }
    }

    override fun saveAll(entities: Iterable<Session>): List<Session> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: Identifier): Session? {
        return sessions[id.value]?.toDomain()
    }

    override fun findAll(): List<Session> {
        return sessions.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<Session> {
        val page = utils.paginate(sessions.values.toList(), pagination)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<Identifier>): List<Session> {
        return sessions.values.filter { it.id in ids.map { id -> id.value } }.map { it.toDomain() }
    }

    override fun deleteById(id: Identifier) {
        if (sessions.containsKey(id.value)) {
            delete(sessions[id.value]!!.toDomain())
        }
    }

    override fun existsById(id: Identifier): Boolean {
        return sessions.containsKey(id.value)
    }

    override fun count(): Long {
        return sessions.size.toLong()
    }

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
