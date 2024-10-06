package im.invitations

import java.time.LocalDateTime
import java.util.*

data class ImInvitation(
    val token: UUID = UUID.randomUUID(),
    val status: ImInvitationStatus = ImInvitationStatus.PENDING,
    val expiresAt: LocalDateTime
) {
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    fun use(): ImInvitation = copy(status = ImInvitationStatus.USED)
}