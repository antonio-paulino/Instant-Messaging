package im.domain.invitations

import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents an invitation to a channel.
 * @property token The unique identifier of the invitation.
 * @property status The status of the invitation.
 * @property expiresAt The date and time when the invitation expires.
 * @property expired Indicates if the invitation has expired.
 */
data class ImInvitation(
    val token: UUID = UUID.randomUUID(),
    val status: ImInvitationStatus = ImInvitationStatus.PENDING,
    val expiresAt: LocalDateTime,
) {
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    fun use(): ImInvitation = copy(status = ImInvitationStatus.USED)
}
