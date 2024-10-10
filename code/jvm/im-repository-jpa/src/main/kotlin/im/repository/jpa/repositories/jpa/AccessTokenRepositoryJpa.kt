package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.token.AccessTokenDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccessTokenRepositoryJpa : JpaRepository<AccessTokenDTO, UUID> {
    @Query("SELECT a FROM AccessTokenDTO a WHERE a.session.id = :sessionId")
    fun findBySession(sessionId: Long): List<AccessTokenDTO>
}