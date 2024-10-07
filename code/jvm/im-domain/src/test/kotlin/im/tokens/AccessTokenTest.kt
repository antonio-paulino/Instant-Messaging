package im.tokens

import im.sessions.Session
import im.user.User
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AccessTokenTest {

    @Test
    fun `should check if AccessToken is expired`() {
        val user = User(1, "user", "password", "user1@daw.isel.pt")
        val session = Session(1, user, LocalDateTime.now().plusDays(1))
        val accessToken = AccessToken(UUID.randomUUID(),session, LocalDateTime.now().plusDays(1))
        assertEquals(false, accessToken.expired)
    }
}