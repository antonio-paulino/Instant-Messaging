package im.wrappers

import im.domain.wrappers.email.toEmail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertTrue(exception.message!!.contains("Email cannot be blank"))
    }

    @Test
    fun `should throw exception for email too short`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "a@b.ct".toEmail()
            }
        assertTrue(exception.message!!.contains("Email must be between 8 and 50 characters"))
    }

    @Test
    fun `should throw exception for email too long`() {
        val longEmail = "a".repeat(41) + "@example.com"
        val exception =
            assertThrows<IllegalArgumentException> {
                longEmail.toEmail()
            }
        assertTrue(exception.message!!.contains("Email must be between 8 and 50 characters"))
    }

    @Test
    fun `should throw exception for invalid email format`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "invalid-email".toEmail()
            }
        assertTrue(exception.message!!.contains("Email has an invalid format"))
    }
}
