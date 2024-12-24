package im.repository.jpa.model.invitation

import im.domain.invitations.ImInvitation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents an invitation in the database.
 *
 * - An invitation has a unique token.
 * - An invitation has an expiration date.
 * - An invitation has a status.
 *
 * @property token The unique token of the invitation.
 * @property expiresAt The date and time when the invitation expires.
 * @property status The status of the invitation.
 */
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
    open val status: ImInvitationStatus = ImInvitationStatus.PENDING,
) {
    companion object {
        fun fromDomain(invitation: ImInvitation) =
            ImInvitationDTO(
                token = invitation.token,
                expiresAt = invitation.expiresAt,
                status = ImInvitationStatus.valueOf(invitation.status.name),
            )
    }

    fun toDomain() =
        ImInvitation(
            token,
            im.domain.invitations.ImInvitationStatus
                .valueOf(status.name),
            expiresAt,
        )
}
