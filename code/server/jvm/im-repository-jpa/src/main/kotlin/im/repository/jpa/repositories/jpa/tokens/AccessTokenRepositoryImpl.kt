package im.repository.jpa.repositories.jpa.tokens

import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.repository.jpa.model.token.AccessTokenDTO
import im.repository.jpa.repositories.jpa.JpaRepositoryUtils
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.tokens.AccessTokenRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Primary
class AccessTokenRepositoryImpl(
    private val accessTokenRepositoryJpa: AccessTokenRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : AccessTokenRepository {
    override fun findBySession(session: Session): List<AccessToken> =
        accessTokenRepositoryJpa.findBySessionId(session.id.value).map { it.toDomain() }

    override fun deleteExpired() {
        accessTokenRepositoryJpa.deleteAllByExpiresAtIsBefore()
    }

    override fun save(entity: AccessToken): AccessToken = accessTokenRepositoryJpa.save(AccessTokenDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<AccessToken>): List<AccessToken> =
        accessTokenRepositoryJpa
            .saveAll(
                entities.map {
                    AccessTokenDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: UUID): AccessToken? = accessTokenRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<AccessToken> = accessTokenRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<AccessToken> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                accessTokenRepositoryJpa.findAll(pageable)
            } else {
                accessTokenRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<AccessToken> = accessTokenRepositoryJpa.findAllById(ids).map { it.toDomain() }

    override fun deleteById(id: UUID) {
        accessTokenRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean = accessTokenRepositoryJpa.existsById(id)

    override fun count(): Long = accessTokenRepositoryJpa.count()

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
        accessTokenRepositoryJpa.flush()
    }
}
