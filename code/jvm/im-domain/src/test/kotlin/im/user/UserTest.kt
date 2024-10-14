package im.user

import im.domain.user.User
import im.domain.wrappers.Identifier
import im.domain.wrappers.toEmail
import im.domain.wrappers.toName
import im.domain.wrappers.toPassword
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class UserTest {
    @Test
    fun `test primary constructor`() {
        val id = Identifier(1)
        val name = "JohnDoe".toName()
        val password = "password123".toPassword()
        val email = "john.doe@example.com".toEmail()

        val user = User(id, name, password, email)

        assertEquals(id, user.id)
        assertEquals(name, user.name)
        assertEquals(password, user.password)
        assertEquals(email, user.email)
    }

    @Test
    fun `test secondary constructor`() {
        val id = 1L
        val name = "JohnDoe"
        val password = "password123"
        val email = "john.doe@example.com"

        val user = User(id, name, password, email)

        assertEquals(Identifier(id), user.id)
        assertEquals(name.toName(), user.name)
        assertEquals(password.toPassword(), user.password)
        assertEquals(email.toEmail(), user.email)
    }
}
