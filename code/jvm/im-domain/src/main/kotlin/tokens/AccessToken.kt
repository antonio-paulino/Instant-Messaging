package tokens

import sessions.Session
import java.time.LocalDateTime
import java.util.UUID

data class AccessToken(
    val token: UUID,
    val session: Session,
    val expiresAt: LocalDateTime
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}