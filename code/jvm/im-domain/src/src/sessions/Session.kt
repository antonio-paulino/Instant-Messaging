package sessions

import user.User
import java.time.LocalDateTime

interface Session {
    val id : Long
    val user: User
    val expiresAt: LocalDateTime
    val accessTokens: List<AccessToken>
    val refreshTokens: List<RefreshToken>
    fun refresh() : Session
}