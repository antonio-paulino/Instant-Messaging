package invitations

import Repository
import java.util.*

interface ImInvitationRepository : Repository<ImInvitation, UUID> {
    fun findByToken(token: UUID): ImInvitation?
}