package im.repository.jpa.repositories

import im.repository.jpa.model.token.AccessTokenDTO
import im.repository.jpa.repositories.jpa.AccessTokenRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import im.tokens.AccessToken
import im.repository.repositories.tokens.AccessTokenRepository
import im.sessions.Session
import org.springframework.context.annotation.Primary
import java.util.*

@Component
@Primary
class AccessTokenRepositoryImpl(
    private val accessTokenRepositoryJpa: AccessTokenRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : AccessTokenRepository {

    override fun findBySession(session: Session): List<AccessToken> {
        return accessTokenRepositoryJpa.findBySession(session.id.value).map { it.toDomain() }
    }

    override fun save(entity: AccessToken): AccessToken {
        return accessTokenRepositoryJpa.save(AccessTokenDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<AccessToken>): List<AccessToken> {
        return accessTokenRepositoryJpa.saveAll(entities.map { AccessTokenDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: UUID): AccessToken? {
        return accessTokenRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<AccessToken> {
        return accessTokenRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<AccessToken> {
        val res = accessTokenRepositoryJpa.findAll(utils.toPageRequest(pagination, "expiresAt"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<AccessToken> {
        return accessTokenRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        accessTokenRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean {
        return accessTokenRepositoryJpa.existsById(id)
    }

    override fun count(): Long {
        return accessTokenRepositoryJpa.count()
    }

    override fun deleteAll() {
        accessTokenRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<AccessToken>) {
        accessTokenRepositoryJpa.deleteAll(entities.map { AccessTokenDTO.fromDomain(it) })
    }

    override fun delete(entity: AccessToken) {
        accessTokenRepositoryJpa.delete(AccessTokenDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        accessTokenRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        entityManager.flush()
        accessTokenRepositoryJpa.flush()
    }

}