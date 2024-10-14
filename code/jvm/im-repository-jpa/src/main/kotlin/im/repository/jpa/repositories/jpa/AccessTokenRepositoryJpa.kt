package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.token.AccessTokenDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID> {
    fun findBySessionId(sessionId: Long): List<AccessTokenDTO>

    fun deleteAllByExpiresAtIsBefore(expiresAt: LocalDateTime = LocalDateTime.now())

    fun findBy(pageable: Pageable): Slice<AccessTokenDTO>
}
