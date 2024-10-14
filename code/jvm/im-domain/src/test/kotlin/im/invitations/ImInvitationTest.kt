package im.invitations

import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ImInvitationTest {
    @Test
    fun `should use invitation`() {
        val uuid = UUID.randomUUID()
        val invitation = ImInvitation(uuid, ImInvitationStatus.PENDING, LocalDateTime.now())
        val usedInvitation = invitation.use()
        assertEquals(ImInvitationStatus.USED, usedInvitation.status)
    }

    @Test
    fun `invitation is expired`() {
        val invitation = ImInvitation(UUID.randomUUID(), ImInvitationStatus.PENDING, LocalDateTime.now().minusDays(1))
        assertEquals(true, invitation.expired)
    }
}
