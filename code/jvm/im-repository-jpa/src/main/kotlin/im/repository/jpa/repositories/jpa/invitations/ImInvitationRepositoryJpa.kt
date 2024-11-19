package im.repository.jpa.repositories.jpa.invitations

import im.repository.jpa.model.invitation.ImInvitationDTO
import im.repository.jpa.model.invitation.ImInvitationStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface ImInvitationRepositoryJpa : JpaRepository<ImInvitationDTO, UUID> {
    fun deleteAllByExpiresAtIsBeforeOrStatusIs(
        expiresAt: LocalDateTime = LocalDateTime.now(),
        status: ImInvitationStatus = ImInvitationStatus.USED,
    )

    fun findBy(pageable: Pageable): Slice<ImInvitationDTO>
}
