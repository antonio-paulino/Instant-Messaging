package im.channel

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.name.toName
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChannelTest {
    @Test
    fun `should update channel`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val channel = Channel(1, "im/channel", user, true)
        val updatedChannel = channel.updateChannel("new channel".toName(), false)
        assertEquals("new channel".toName(), updatedChannel.name)
        assertFalse(updatedChannel.isPublic)
    }

    @Test
    fun `should add member`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", user, true)
        val updatedChannel = channel.addMember(user2, ChannelRole.MEMBER)
        assertEquals(2, updatedChannel.members.size)
        assertEquals(ChannelRole.MEMBER, updatedChannel.members[user2])
        assertTrue { channel.hasMember(user) }
    }

    @Test
    fun `should remove member`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", user, true)
        val updatedChannel = channel.addMember(user2, ChannelRole.MEMBER)
        val updatedChannel2 = updatedChannel.removeMember(user2)
        assertEquals(1, updatedChannel2.members.size)
    }

    @Test
    fun `should change the role of a member`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", user, true)
        val updatedChannel = channel.addMember(user2, ChannelRole.MEMBER)
        val updatedChannel2 = updatedChannel.addMember(user2, ChannelRole.OWNER)
        assertEquals(2, updatedChannel2.members.size)
        assertEquals(ChannelRole.OWNER, updatedChannel2.members[user2])
    }

    @Test
    fun `test equal channels`() {
        val user = User(1, "user", "Password123", "user1@daw.isel.pt")
        val creation = LocalDateTime.now()
        val channel = Channel(1, "im/channel", user, true, createdAt = creation)
        val channel2 = Channel(1, "im/channel", user, true, createdAt = creation)
        assertEquals(channel, channel2)
        assertEquals(channel.hashCode(), channel2.hashCode())
    }
}
