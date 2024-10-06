package im.tokens

import im.sessions.Session
import java.time.LocalDateTime
import java.util.*

data class RefreshToken(
    val token: UUID = UUID.randomUUID(),
    val session: Session
) {
    val expired: Boolean
        get() = session.expiresAt.isBefore(LocalDateTime.now())
}