package im.invitations

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChannelInvitationTest {
    @Test
    fun `should accept invitation`() {
        val user1 = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", ChannelRole.MEMBER, user1, true)

        val invitation =
            ChannelInvitation(
                1,
                channel,
                user1,
                user2,
                ChannelInvitationStatus.PENDING,
                ChannelRole.MEMBER,
                LocalDateTime.now(),
            )
        val acceptedInvitation = invitation.accept()

        assertEquals(ChannelInvitationStatus.ACCEPTED, acceptedInvitation.status)
    }

    @Test
    fun `should reject invitation`() {
        val user1 = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", ChannelRole.MEMBER, user1, true)

        val invitation =
            ChannelInvitation(
                1,
                channel,
                user1,
                user2,
                ChannelInvitationStatus.PENDING,
                ChannelRole.MEMBER,
                LocalDateTime.now(),
            )
        val rejectedInvitation = invitation.reject()

        assertEquals(ChannelInvitationStatus.REJECTED, rejectedInvitation.status)
    }

    @Test
    fun `should update invitation`() {
        val user1 = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", ChannelRole.MEMBER, user1, true)

        val time = LocalDateTime.now()
        val invitation =
            ChannelInvitation(
                1,
                channel,
                user1,
                user2,
                ChannelInvitationStatus.PENDING,
                ChannelRole.MEMBER,
                time,
            )
        val updatedInvitation = invitation.update(ChannelRole.GUEST, time.plusDays(1))

        assertEquals(ChannelRole.GUEST, updatedInvitation.role)
        assertEquals(time.plusDays(1), updatedInvitation.expiresAt)
    }

    @Test
    fun `test create invite inviter not in channel`() {
        val user1 = User(1, "user", "Password123", "user1@daw.isel.pt")
        val user2 = User(2, "user2", "Password123", "user2@daw.isel.pt")
        val channel = Channel(1, "im/channel", ChannelRole.MEMBER, user1, true)

        assertFailsWith<IllegalArgumentException> {
            ChannelInvitation(
                1,
                channel,
                user2,
                user1,
                ChannelInvitationStatus.PENDING,
                ChannelRole.MEMBER,
                LocalDateTime.now(),
            )
        }
    }
}
