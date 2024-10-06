package im.sessions

import im.user.User
import java.time.LocalDateTime

data class Session(
    val id: Long = 0,
    val user: User,
    val expiresAt: LocalDateTime
) {
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    fun refresh(newExpiresAt: LocalDateTime): Session = copy(expiresAt = newExpiresAt)
}