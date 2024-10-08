package im.sessions

import im.user.User
import im.wrappers.Identifier
import im.wrappers.toIdentifier
import java.time.LocalDateTime

data class Session(
    val id: Identifier = Identifier(0),
    val user: User,
    val expiresAt: LocalDateTime
) {
    companion object {
        operator fun invoke(id: Long, user: User, expiresAt: LocalDateTime): Session {
            return Session(
                id = id.toIdentifier(),
                user = user,
                expiresAt = expiresAt
            )
        }
    }
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    fun refresh(newExpiresAt: LocalDateTime): Session = copy(expiresAt = newExpiresAt)
}