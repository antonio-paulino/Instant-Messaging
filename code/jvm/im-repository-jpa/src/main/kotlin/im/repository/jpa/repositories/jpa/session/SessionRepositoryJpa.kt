package im.repository.jpa.repositories.jpa.session

import im.repository.jpa.model.session.SessionDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SessionRepositoryJpa : JpaRepository<SessionDTO, Long> {
    fun findByUserId(userId: Long): List<SessionDTO>

    fun deleteAllByExpiresAtIsBefore(expiresAt: LocalDateTime = LocalDateTime.now())

    fun findBy(pageable: Pageable): Slice<SessionDTO>
}
