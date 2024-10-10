package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.token.RefreshTokenDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepositoryJpa : JpaRepository<RefreshTokenDTO, UUID> {
    @Query("SELECT r FROM RefreshTokenDTO r WHERE r.session.id = :sessionId")
    fun findBySession(sessionId: Long): List<RefreshTokenDTO>
}
