package im.repositories.jpa

import im.model.session.SessionDTO
import im.model.token.AccessTokenDTO
import im.model.token.RefreshTokenDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SessionRepositoryJpa : JpaRepository<SessionDTO, Long> {
    @Query("SELECT a FROM AccessTokenDTO a WHERE a.session.id = :sessionId")
    fun findAccessTokensBySession(sessionId: Long): List<AccessTokenDTO>

    @Query("SELECT r FROM RefreshTokenDTO r WHERE r.session.id = :sessionId")
    fun findRefreshTokensBySession(sessionId: Long): List<RefreshTokenDTO>
}