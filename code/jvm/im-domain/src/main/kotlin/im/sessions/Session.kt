package im.sessions

import im.user.User
import im.wrappers.Identifier
import java.time.LocalDateTime

/**
 * Represents a session for a user.
 *
 * @property id The unique identifier of the session.
 * @property user The user that owns the session.
 * @property expiresAt The date and time when the session expires.
 * @property expired Indicates if the session has expired.
 */
data class Session(
    val id: Identifier = Identifier(0),
    val user: User,
    val expiresAt: LocalDateTime
) {

    constructor(id: Long, user: User, expiresAt: LocalDateTime) : this(
        id = Identifier(id),
        user = user,
        expiresAt = expiresAt
    )

    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    /**
     * Refreshes the session by setting a new expiration date.
     *
     * @param newExpiresAt the new expiration date
     * @return a new session with the updated expiration date
     */
    fun refresh(newExpiresAt: LocalDateTime): Session = copy(expiresAt = newExpiresAt)
}