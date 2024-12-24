package im.repository.mem.model.invitation

import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
import java.time.LocalDateTime
import java.util.UUID

data class ImInvitationDTO(
    val token: UUID = UUID.randomUUID(),
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),
    val status: ImInvitationStatus = ImInvitationStatus.PENDING,
) {
    companion object {
        fun fromDomain(invitation: ImInvitation) =
            ImInvitationDTO(
                token = invitation.token,
                expiresAt = invitation.expiresAt,
                status = invitation.status,
            )
    }

    fun toDomain() = ImInvitation(token, status, expiresAt)
}
