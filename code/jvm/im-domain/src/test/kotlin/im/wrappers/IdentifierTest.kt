package im.wrappers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IdentifierTest {

    @Test
    fun `should create valid identifier from long`() {
        val identifier = 123L.toIdentifier()
        assertEquals(123L, identifier.value)
    }

    @Test
    fun `should create valid identifier from int`() {
        val identifier = 123.toIdentifier()
        assertEquals(123L, identifier.value)
    }

    @Test
    fun `should throw exception for negative identifier`() {
        val exception = assertThrows<IllegalArgumentException> {
            (-1L).toIdentifier()
        }
        assertEquals("Identifier value must be positive", exception.message)
    }

    @Test
    fun `should return string representation of identifier`() {
        val identifier = 123L.toIdentifier()
        assertEquals("123", identifier.toString())
    }
}