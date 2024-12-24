package im.message

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.messages.Message
import im.domain.user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MessageTest {
    @Test
    fun `should create a message`() {
        val user =
            User(
                id = 1L,
                name = "user",
                password = "Password123",
                email = "user1@daw.isel.pt",
            )
        val channel =
            Channel(
                id = 1L,
                name = "im/channel",
                ChannelRole.MEMBER,
                owner = user,
                isPublic = true,
            )
        val message =
            Message(
                id = 1L,
                channelId = channel.id.value,
                user = user,
                content = "Test message",
                createdAt = LocalDateTime.now(),
            )
        assertEquals("Test message", message.content)
    }

    @Test
    fun `should edit a message`() {
        val user =
            User(
                id = 1L,
                name = "user",
                password = "Password123",
                email = "user1@daw.isel.pt",
            )
        val channel =
            Channel(
                id = 1L,
                name = "im/channel",
                ChannelRole.MEMBER,
                owner = user,
                isPublic = true,
            )
        val message =
            Message(
                id = 1L,
                channelId = channel.id.value,
                user = user,
                content = "Test message",
                createdAt = LocalDateTime.now(),
            )
        assertEquals("Test message", message.content)
        val editedMessage = message.edit("Edited message")
        assertEquals("Edited message", editedMessage.content)
    }

    @Test
    fun `test create message blank content`() {
        val user =
            User(
                id = 1L,
                name = "user",
                password = "Password123",
                email = "user1@daw.isel.pt",
            )
        val channel =
            Channel(
                id = 1L,
                name = "im/channel",
                ChannelRole.MEMBER,
                owner = user,
                isPublic = true,
            )
        assertFailsWith<IllegalArgumentException> {
            Message(
                id = 1L,
                channelId = channel.id.value,
                user = user,
                content = "",
                createdAt = LocalDateTime.now(),
            )
        }
    }

    @Test
    fun `test create message too long`() {
        val user =
            User(
                id = 1L,
                name = "user",
                password = "Password123",
                email = "user1@daw.isel.pt",
            )
        val channel =
            Channel(
                id = 1L,
                name = "im/channel",
                ChannelRole.MEMBER,
                owner = user,
                isPublic = true,
            )
        assertFailsWith<IllegalArgumentException> {
            Message(
                id = 1L,
                channelId = channel.id.value,
                user = user,
                content = "a".repeat(301),
                createdAt = LocalDateTime.now(),
            )
        }
    }
}
