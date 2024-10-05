package repositories

import jakarta.persistence.EntityManager
import model.token.AccessTokenDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import tokens.AccessToken
import tokens.AccessTokenRepository
import java.util.*

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID>

@Component
class AccessTokenRepositoryImpl(
    private val accessTokenRepositoryJpa: AccessTokenRepositoryJpa,
    private val entityManager: EntityManager
) : AccessTokenRepository {

    override fun save(entity: AccessToken): AccessToken {
        return accessTokenRepositoryJpa.save(AccessTokenDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<AccessToken>): List<AccessToken> {
        return accessTokenRepositoryJpa.saveAll(entities.map { AccessTokenDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: UUID): Optional<AccessToken> {
        return accessTokenRepositoryJpa.findById(id).map { it.toDomain() }
    }

    override fun findAll(): List<AccessToken> {
        return accessTokenRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun findFirst(page: Int, pageSize: Int): List<AccessToken> {
        val res = accessTokenRepositoryJpa.findAll(Pageable.ofSize(pageSize).withPage(page))
        return res.content.map { it.toDomain() }
    }

    override fun findLast(page: Int, pageSize: Int): List<AccessToken> {
        val query = entityManager.createQuery(
            "SELECT u FROM AccessTokenDTO u ORDER BY u.expiresAt DESC",
            AccessTokenDTO::class.java
        )
        query.firstResult = page * pageSize
        query.maxResults = pageSize
        return query.resultList.map { it.toDomain() }
    }

    override fun findAllById(ids: Iterable<UUID>): Iterable<AccessToken> {
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