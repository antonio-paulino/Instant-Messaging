package im.wrappers

import org.junit.jupiter.api.Assertions.*
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
        val exception = assertThrows<IllegalArgumentException> {
            "".toPassword()
        }
        assertEquals("Password cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for password too short`() {
        val exception = assertThrows<IllegalArgumentException> {
            "short".toPassword()
        }
        assertEquals("Password must be between 8 and 80 characters", exception.message)
    }

    @Test
    fun `should throw exception for password too long`() {
        val longPassword = "a".repeat(81)
        val exception = assertThrows<IllegalArgumentException> {
            longPassword.toPassword()
        }
        assertEquals("Password must be between 8 and 80 characters", exception.message)
    }

    @Test
    fun `should return string representation of password`() {
        val password = "validPassword123".toPassword()
        assertEquals("validPassword123", password.toString())
    }
}