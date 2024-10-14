package im.domain.tokens

import im.domain.sessions.Session
import java.time.LocalDateTime
import java.util.UUID

/**
 * An access token that can be used to authenticate requests.
 *
 * @property token The unique identifier of the access token.
 * @property session The session associated with the access token.
 * @property expiresAt The date and time when the access token expires.
 * @property expired Indicates if the access token has expired.
 */
data class AccessToken(
    val token: UUID = UUID.randomUUID(),
    val session: Session,
    val expiresAt: LocalDateTime,
) {
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())
}
