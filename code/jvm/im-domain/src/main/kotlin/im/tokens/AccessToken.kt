package im.tokens

import im.sessions.Session
import java.time.LocalDateTime
import java.util.UUID

data class AccessToken(
    val token: UUID = UUID.randomUUID(),
    val session: Session,
    val expiresAt: LocalDateTime
) {
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())
}