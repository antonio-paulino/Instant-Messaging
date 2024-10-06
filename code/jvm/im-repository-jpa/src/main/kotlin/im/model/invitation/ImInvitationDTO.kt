package im.model.invitation

import im.invitations.ImInvitation
import im.invitations.ImInvitationStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "im_invitation")
open class ImInvitationDTO(
    @Id
    @Column(nullable = false, length = 32)
    open val token: UUID = UUID.randomUUID(),

    @Column(name = "expires_at", nullable = false)
    open val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7),

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    open val status: ImInvitationStatus = ImInvitationStatus.PENDING
) {
    companion object {
        fun fromDomain(invitation: ImInvitation) = ImInvitationDTO(
            token = invitation.token,
            expiresAt = invitation.expiresAt,
            status = invitation.status
        )
    }

    fun toDomain() = ImInvitation(token, status, expiresAt)
}

