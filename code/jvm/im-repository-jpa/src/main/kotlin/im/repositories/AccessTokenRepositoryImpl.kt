package im.repositories

import im.pagination.Pagination
import im.model.token.AccessTokenDTO
import im.pagination.PaginationRequest
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import im.tokens.AccessToken
import im.repositories.tokens.AccessTokenRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.*

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID>

internal fun <T> Page<T>.getPagination(pageable: Pageable): Pagination {
    val currentPage = pageable.pageNumber + 1 // Page starts at 0, Pagination at 1
    val nextPage = if (hasNext()) currentPage + 1 else null
    val prevPage = if (hasPrevious()) currentPage - 1 else null
    return Pagination(totalElements, currentPage, totalPages, nextPage, prevPage)
}

internal fun im.pagination.Sort?.toSpringSort(): Sort.Direction {
    return when (this) {
        im.pagination.Sort.ASC -> Sort.Direction.ASC
        im.pagination.Sort.DESC -> Sort.Direction.DESC
        null -> Sort.Direction.ASC
    }
}

internal fun PaginationRequest.toPageRequest(property: String): PageRequest {
    return PageRequest.of(page - 1, size, sort.toSpringSort(), property)
}

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

    override fun findById(id: UUID): AccessToken? {
        return accessTokenRepositoryJpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<AccessToken> {
        return accessTokenRepositoryJpa.findAll().map { it.toDomain() }
    }

    override fun find(pagination: PaginationRequest): Pair<List<AccessToken>, Pagination> {
        val res = accessTokenRepositoryJpa.findAll(pagination.toPageRequest("expiresAt"))
        return res.content.map { it.toDomain() } to res.getPagination(res.pageable)
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