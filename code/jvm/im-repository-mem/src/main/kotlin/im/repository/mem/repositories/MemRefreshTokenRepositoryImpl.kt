package im.repository.mem.repositories

import im.domain.sessions.Session
import im.domain.tokens.RefreshToken
import im.repository.mem.model.token.RefreshTokenDTO
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.tokens.RefreshTokenRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MemRefreshTokenRepositoryImpl(
    private val utils: MemRepoUtils,
) : RefreshTokenRepository {
    private val refreshTokens = ConcurrentHashMap<UUID, RefreshTokenDTO>()

    override fun findBySession(session: Session): List<RefreshToken> =
        refreshTokens.values
            .filter {
                it.session.toDomain() == session
            }.map { it.toDomain() }

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

    override fun findById(id: UUID): RefreshToken? = refreshTokens[id]?.toDomain()

    override fun findAll(): List<RefreshToken> = refreshTokens.values.map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<RefreshToken> {
        val page = utils.paginate(refreshTokens.values.toList(), pagination, sortRequest, pagination.getCount)
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<UUID>): List<RefreshToken> =
        refreshTokens.values
            .filter {
                it.token in ids
            }.map { it.toDomain() }

    override fun deleteById(id: UUID) {
        if (refreshTokens.containsKey(id)) {
            delete(refreshTokens[id]!!.toDomain())
        }
    }

    override fun existsById(id: UUID): Boolean = refreshTokens.containsKey(id)

    override fun count(): Long = refreshTokens.size.toLong()

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
