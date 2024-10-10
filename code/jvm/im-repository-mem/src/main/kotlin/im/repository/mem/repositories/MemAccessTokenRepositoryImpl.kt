package im.repository.mem.repositories

import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.tokens.AccessTokenRepository
import im.repository.mem.model.token.AccessTokenDTO
import im.sessions.Session
import im.tokens.AccessToken
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class MemAccessTokenRepositoryImpl(
    private val utils: MemRepoUtils
) : AccessTokenRepository {

    private val accessTokens = ConcurrentHashMap<UUID, AccessTokenDTO>()

    override fun save(entity: AccessToken): AccessToken {
        val conflict = accessTokens.values.find { it.token == entity.token }
        if (conflict != null) {
            accessTokens[entity.token] = AccessTokenDTO.fromDomain(entity)
            return entity
        } else {
            accessTokens[entity.token] = AccessTokenDTO.fromDomain(entity)
            return entity
        }
    }

    override fun findBySession(session: Session): List<AccessToken> {
        return accessTokens.values.filter { it.session.id == session.id.value }.map { it.toDomain() }
    }

    override fun saveAll(entities: Iterable<AccessToken>): List<AccessToken> {
        val newEntities = entities.map { save(it) }
        return newEntities.toList()
    }

    override fun findById(id: UUID): AccessToken? {
        return accessTokens[id]?.toDomain()
    }

    override fun findAll(): List<AccessToken> {
        return accessTokens.values.map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<AccessToken> {
        val page = utils.paginate(accessTokens.values.toList(), pagination, "expiresAt")
        return Pagination(page.items.map { it.toDomain() }, page.info)
    }

    override fun findAllById(ids: Iterable<UUID>): List<AccessToken> {
        return accessTokens.values.filter { it.token in ids }.map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        if (accessTokens.containsKey(id)) {
            accessTokens.remove(id)
        }
    }

    override fun existsById(id: UUID): Boolean {
        return accessTokens.containsKey(id)
    }

    override fun count(): Long {
        return accessTokens.size.toLong()
    }

    override fun deleteAll() {
        accessTokens.forEach { delete(it.value.toDomain()) }
    }

    override fun deleteAll(entities: Iterable<AccessToken>) {
        entities.forEach { delete(it) }
    }

    override fun delete(entity: AccessToken) {
        accessTokens.remove(entity.token)
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        ids.forEach { deleteById(it) }
    }

    fun deleteAllBySession(session: Session) {
        accessTokens.values.filter { it.session.id == session.id.value }.forEach { delete(it.toDomain()) }
    }

    override fun flush() {
        // no-op
    }
}