package im.tokens

import im.domain.sessions.Session
import im.domain.user.User
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class RefreshTokenTest {
    @Test
    fun `should check if RefreshToken is expired`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val session = Session(1, user, LocalDateTime.now().plusDays(1))
        val refreshToken = im.domain.tokens.RefreshToken(UUID.randomUUID(), session)
        assertEquals(false, refreshToken.expired)
    }
}
