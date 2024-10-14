package im.wrappers

import im.domain.wrappers.toName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NameTest {
    @Test
    fun `should create valid name`() {
        val name = "validName".toName()
        assertEquals("validName", name.value)
    }

    @Test
    fun `should throw exception for blank name`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "".toName()
            }
        assertEquals("Name cannot be blank", exception.message)
    }

    @Test
    fun `should throw exception for name too short`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                "ab".toName()
            }
        assertEquals("Name must be between 3 and 30 characters", exception.message)
    }

    @Test
    fun `should throw exception for name too long`() {
        val longName = "a".repeat(31)
        val exception =
            assertThrows<IllegalArgumentException> {
                longName.toName()
            }
        assertEquals("Name must be between 3 and 30 characters", exception.message)
    }

    @Test
    fun `should return string representation of name`() {
        val name = "validName".toName()
        assertEquals("validName", name.toString())
    }
}
