package invitations

import Repository
import java.util.*

/**
 * [Repository] for [ImInvitation] entities.
 */
interface ImInvitationRepository : Repository<ImInvitation, UUID> {
    /**
     * Finds an invitation by its token.
     *
     * @param token the token of the invitation
     * @return the invitation with the given token, or `null` if no such invitation exists
     */
    fun findByToken(token: UUID): ImInvitation?
}