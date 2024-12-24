package im.wrappers

import im.domain.wrappers.password.toPassword
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PasswordTest {
    @Test
    fun `should create valid password`() {
        val password = "validPassword123".toPassword()
        assertEquals("validPassword123", password.value)
    }

    @Test
    fun `should throw exception for blank password`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "".toPassword()
            }
        assertTrue(exception.message!!.contains("Password cannot be blank"))
    }

    @Test
    fun `should throw exception for password too short`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "short".toPassword()
            }
        assertTrue(exception.message!!.contains("Password must be between 8 and 80 characters"))
    }

    @Test
    fun `should throw exception for password too long`() {
        val longPassword = "a".repeat(81)
        val exception =
            assertThrows<IllegalArgumentException> {
                longPassword.toPassword()
            }
        assertTrue(exception.message!!.contains("Password must be between 8 and 80 characters"))
    }

    @Test
    fun `should return string representation of password`() {
        val password = "validPassword123".toPassword()
        assertEquals("validPassword123", password.toString())
    }
}
