package invitations

import java.time.LocalDateTime
import java.util.*

interface ImInvitation {
    val token: UUID
    val status: ImInvitationStatus
    val expirationDate: LocalDateTime

    fun use() : ImInvitation
}