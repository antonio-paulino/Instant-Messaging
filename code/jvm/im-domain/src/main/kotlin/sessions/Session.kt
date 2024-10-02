package sessions

import user.User
import java.time.LocalDateTime

const val SESSION_DURATION_DAYS = 90L

data class Session(
    val id: Long = 0,
    val user: User,
    val expiresAt: LocalDateTime,
) {
    fun refresh(): Session = copy(expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS))
}