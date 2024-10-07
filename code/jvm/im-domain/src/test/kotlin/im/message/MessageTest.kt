package im.message

import im.channel.Channel
import im.messages.Message
import im.user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {

    @Test
    fun `should create a message`() {
        val user = User(
            id = 1L,
            name = "user",
            password = "password",
            email = "user1@daw.isel.pt"
        )
        val channel = Channel(
            id = 1L,
            name = "im/channel",
            owner = user,
            isPublic = true
        )
        val message = Message(
            id = 1L,
            channel = channel,
            user = user,
            content = "Test message",
            createdAt = LocalDateTime.now()
        )
        assertEquals("Test message", message.content)
    }

    @Test
    fun `should edit a message`() {
        val user = User(
            id = 1L,
            name = "user",
            password = "password",
            email = "user1@daw.isel.pt"
        )
        val channel = Channel(
            id = 1L,
            name = "im/channel",
            owner = user,
            isPublic = true
        )
        val message = Message(
            id = 1L,
            channel = channel,
            user = user,
            content = "Test message",
            createdAt = LocalDateTime.now()
        )
        assertEquals("Test message", message.content)
        val editedMessage = message.edit("Edited message")
        assertEquals("Edited message", editedMessage.content)
    }
}