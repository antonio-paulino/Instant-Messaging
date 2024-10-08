package im.sessions

import im.user.User
import java.time.LocalDateTime

data class Session(
    val id: Long = 0,
    val user: User,
    val expiresAt: LocalDateTime
) {
    init {
        require(id >= 0) { "Session ID must be positive" }
    }
    val expired: Boolean
        get() = expiresAt.isBefore(LocalDateTime.now())

    fun refresh(newExpiresAt: LocalDateTime): Session = copy(expiresAt = newExpiresAt)
}