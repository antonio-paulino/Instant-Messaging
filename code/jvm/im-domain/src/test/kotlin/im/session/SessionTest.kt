package im.session

import im.domain.sessions.Session
import im.domain.user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTest {
    @Test
    fun `should create a session`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val session = Session(1, user, LocalDateTime.now().plusDays(1))
        assertEquals(user, session.user)
    }

    @Test
    fun `should refresh session`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val session = Session(1, user, LocalDateTime.now().plusDays(1))
        val newExpiresAt = LocalDateTime.now().plusDays(2)
        val refreshedSession = session.refresh(newExpiresAt)
        assertEquals(newExpiresAt, refreshedSession.expiresAt)
        assertEquals(user, refreshedSession.user)
    }

    @Test
    fun `should check if session is expired`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val session = Session(1, user, LocalDateTime.now().minusDays(1))
        assertEquals(true, session.expired)
    }
}
