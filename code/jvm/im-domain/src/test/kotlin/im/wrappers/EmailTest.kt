package im.wrappers

import im.domain.wrappers.toEmail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmailTest {
    @Test
    fun `should create valid email`() {
        val email = "test@example.com".toEmail()
        assertEquals("test@example.com", email.value)
    }

    @Test
    fun `should throw exception for blank email`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "".toEmail()
            }
        assertEquals("User email cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for email too short`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "a@b.ct".toEmail()
            }
        assertEquals("User email must be between 8 and 50 characters", exception.message)
    }

    @Test
    fun `should throw exception for email too long`() {
        val longEmail = "a".repeat(41) + "@example.com"
        val exception =
            assertThrows<IllegalArgumentException> {
                longEmail.toEmail()
            }
        assertEquals("User email must be between 8 and 50 characters", exception.message)
    }

    @Test
    fun `should throw exception for invalid email format`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "invalid-email".toEmail()
            }
        assertEquals("User email must be a valid email address", exception.message)
    }
}
