package im.repository.jpa.repositories.jpa.tokens

import im.domain.sessions.Session
import im.domain.tokens.RefreshToken
import im.repository.jpa.model.token.RefreshTokenDTO
import im.repository.jpa.repositories.jpa.JpaRepositoryUtils
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.tokens.RefreshTokenRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Primary
class RefreshTokenRepositoryImpl(
    private val refreshTokenRepositoryJpa: RefreshTokenRepositoryJpa,
    private val utils: JpaRepositoryUtils,
) : RefreshTokenRepository {
    override fun findBySession(session: Session): List<RefreshToken> =
        refreshTokenRepositoryJpa.findBySessionId(session.id.value).map { it.toDomain() }

    override fun save(entity: RefreshToken): RefreshToken = refreshTokenRepositoryJpa.save(RefreshTokenDTO.fromDomain(entity)).toDomain()

    override fun saveAll(entities: Iterable<RefreshToken>): List<RefreshToken> =
        refreshTokenRepositoryJpa
            .saveAll(
                entities.map {
                    RefreshTokenDTO.fromDomain(it)
                },
            ).map { it.toDomain() }

    override fun findById(id: UUID): RefreshToken? = refreshTokenRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<RefreshToken> = refreshTokenRepositoryJpa.findAll().map { it.toDomain() }

    override fun find(
        pagination: PaginationRequest,
        sortRequest: SortRequest,
    ): Pagination<RefreshToken> {
        val pageable = utils.toPageRequest(pagination, sortRequest)
        val res =
            if (pagination.getCount) {
                refreshTokenRepositoryJpa.findAll(pageable)
            } else {
                refreshTokenRepositoryJpa.findBy(pageable)
            }
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<RefreshToken> =
        refreshTokenRepositoryJpa.findAllById(ids).map {
            it.toDomain()
        }

    override fun deleteById(id: UUID) {
        refreshTokenRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean = refreshTokenRepositoryJpa.existsById(id)

    override fun count(): Long = refreshTokenRepositoryJpa.count()

    override fun deleteAll() {
        refreshTokenRepositoryJpa.deleteAll()
    }

    override fun deleteAll(entities: Iterable<RefreshToken>) {
        refreshTokenRepositoryJpa.deleteAll(entities.map { RefreshTokenDTO.fromDomain(it) })
    }

    override fun delete(entity: RefreshToken) {
        refreshTokenRepositoryJpa.delete(RefreshTokenDTO.fromDomain(entity))
    }

    override fun deleteAllById(ids: Iterable<UUID>) {
        refreshTokenRepositoryJpa.deleteAllById(ids)
    }

    override fun flush() {
        refreshTokenRepositoryJpa.flush()
    }
}
