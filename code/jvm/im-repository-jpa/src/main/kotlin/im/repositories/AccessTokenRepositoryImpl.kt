package im.repositories

import im.pagination.Pagination
import im.model.token.AccessTokenDTO
import im.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import im.tokens.AccessToken
import im.repositories.tokens.AccessTokenRepository
import java.util.*

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID>

@Component
class AccessTokenRepositoryImpl(
    private val accessTokenRepositoryJpa: AccessTokenRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : AccessTokenRepository {

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