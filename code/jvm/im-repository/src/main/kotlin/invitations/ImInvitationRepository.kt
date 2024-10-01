package invitations

import Repository
import java.util.*

interface ImInvitationRepository : Repository<ImInvitation, Long> {
    fun findByToken(token: UUID): ImInvitation?
}