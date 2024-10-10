package im.repository.jpa.repositories


import im.repository.jpa.model.token.RefreshTokenDTO
import im.repository.jpa.repositories.jpa.RefreshTokenRepositoryJpa
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.repositories.tokens.RefreshTokenRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import im.tokens.RefreshToken
import im.sessions.Session
import org.springframework.context.annotation.Primary
import java.util.*

@Component
@Primary
class RefreshTokenRepositoryImpl(
    private val refreshTokenRepositoryJpa: RefreshTokenRepositoryJpa,
    private val entityManager: EntityManager,
    private val utils: JpaRepositoryUtils
) : RefreshTokenRepository {

    override fun findBySession(session: Session): List<RefreshToken> {
        return refreshTokenRepositoryJpa.findBySession(session.id.value).map { it.toDomain() }
    }

    override fun save(entity: RefreshToken): RefreshToken {
        return refreshTokenRepositoryJpa.save(RefreshTokenDTO.fromDomain(entity)).toDomain()
    }

    override fun saveAll(entities: Iterable<RefreshToken>): List<RefreshToken> {
        return refreshTokenRepositoryJpa.saveAll(entities.map { RefreshTokenDTO.fromDomain(it) }).map { it.toDomain() }
    }

    override fun findById(id: UUID): RefreshToken? {
        return refreshTokenRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<RefreshToken> {
        return refreshTokenRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pagination<RefreshToken> {
        val res = refreshTokenRepositoryJpa.findAll(utils.toPageRequest(pagination, "token"))
        return Pagination(res.content.map { it.toDomain() }, utils.getPaginationInfo(res))
    }

    override fun findAllById(ids: Iterable<UUID>): List<RefreshToken> {
        return refreshTokenRepositoryJpa.findAllById(ids).map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        refreshTokenRepositoryJpa.deleteById(id)
    }

    override fun existsById(id: UUID): Boolean {
        return refreshTokenRepositoryJpa.existsById(id)
    }

    override fun count(): Long {
        return refreshTokenRepositoryJpa.count()
    }

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
        entityManager.flush()
        refreshTokenRepositoryJpa.flush()
    }
}