package im.repository.jpa.repositories.jpa.tokens

import im.repository.jpa.model.token.RefreshTokenDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepositoryJpa : JpaRepository<RefreshTokenDTO, UUID> {
    fun findBySessionId(sessionId: Long): List<RefreshTokenDTO>

    fun findBy(pageable: Pageable): Slice<RefreshTokenDTO>
}
