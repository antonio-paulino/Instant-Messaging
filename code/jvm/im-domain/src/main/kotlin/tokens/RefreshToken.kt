package tokens

import sessions.Session
import java.time.LocalDateTime
import java.util.*

data class RefreshToken(
    val token: UUID,
    val session: Session
) {
    fun isExpired(): Boolean = session.expiresAt.isBefore(LocalDateTime.now())
}