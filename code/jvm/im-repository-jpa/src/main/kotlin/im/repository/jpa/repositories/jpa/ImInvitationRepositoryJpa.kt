package im.repository.jpa.repositories.jpa

import im.repository.jpa.model.invitation.ImInvitationDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ImInvitationRepositoryJpa : JpaRepository<ImInvitationDTO, UUID>