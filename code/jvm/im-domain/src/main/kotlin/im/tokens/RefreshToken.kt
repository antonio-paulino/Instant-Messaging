package im.tokens

import im.sessions.Session
import java.time.LocalDateTime
import java.util.*

/**
 * A refresh token that can be used to obtain a new access token.
 * @property token The unique identifier of the refresh token.
 * @property session The session associated with the refresh token.
 */
data class RefreshToken(
    val token: UUID = UUID.randomUUID(),
    val session: Session
) {
    val expired: Boolean
        get() = session.expiresAt.isBefore(LocalDateTime.now())
}