package invitations

import java.time.LocalDateTime
import java.util.*

data class ImInvitation(
    val token: UUID,
    val status: ImInvitationStatus,
    val expiresAt: LocalDateTime
) {
    fun use(): ImInvitation = copy(status = ImInvitationStatus.USED)
}