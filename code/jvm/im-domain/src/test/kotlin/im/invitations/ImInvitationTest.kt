package im.invitations

import java.time.LocalDateTime
import java.util.*
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
}