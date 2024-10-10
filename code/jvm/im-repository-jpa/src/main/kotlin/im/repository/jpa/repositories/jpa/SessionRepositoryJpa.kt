package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.session.SessionDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SessionRepositoryJpa : JpaRepository<SessionDTO, Long> {
    @Query("SELECT s FROM SessionDTO s WHERE s.user.id = :userId")
    fun findByUser(userId: Long): List<SessionDTO>
}