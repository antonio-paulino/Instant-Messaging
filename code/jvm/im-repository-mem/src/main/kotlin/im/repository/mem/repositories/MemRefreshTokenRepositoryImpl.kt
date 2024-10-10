package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.tokens.RefreshTokenRepository
import im.repository.mem.model.token.RefreshTokenDTO
import im.sessions.Session
import im.tokens.RefreshToken
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MemRefreshTokenRepositoryImpl(
    private val utils: MemRepoUtils
) : RefreshTokenRepository {

    private val refreshTokens = ConcurrentHashMap<UUID, RefreshTokenDTO>()

    override fun findBySession(session: Session): List<RefreshToken> {
        return refreshTokens.values.filter { it.session.toDomain() == session }.map { it.toDomain() }
    }

    override fun save(entity: RefreshToken): RefreshToken {
        val conflict = refreshTokens.values.find { it.token == entity.token }
        if (conflict != null) {
            refreshTokens[entity.token] = RefreshTokenDTO.fromDomain(entity)
            return entity
        } else {
            refreshTokens[entity.token] = RefreshTokenDTO.fromDomain(entity)
            return entity
        }
    }

    override fun saveAll(entities: Iterable<RefreshToken>): List<RefreshToken> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: UUID): RefreshToken? {
        return refreshTokens[id]?.toDomain()
    }

    override fun findAll(): List<RefreshToken> {
        return refreshTokens.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<RefreshToken> {
        val page = utils.paginate(refreshTokens.values.toList(), pagination, "token")
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<UUID>): List<RefreshToken> {
        return refreshTokens.values.filter { it.token in ids }.map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        if (refreshTokens.containsKey(id)) {
            delete(refreshTokens[id]!!.toDomain())
        }
    }

    override fun existsById(id: UUID): Boolean {
        return refreshTokens.containsKey(id)
    }

    override fun count(): Long {
        return refreshTokens.size.toLong()
    }

    override fun deleteAll() {
        refreshTokens.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<RefreshToken>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: RefreshToken) {
        refreshTokens.remove(entity.token)
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        ids.forEach { deleteById(it) }
    }

    fun deleteAllBySession(session: Session) {
        refreshTokens.values.filter { it.session.id == session.id.value }.forEach { delete(it.toDomain()) }
    }

    override fun flush() {
        // No-op for an in-memory repository
    }
}
