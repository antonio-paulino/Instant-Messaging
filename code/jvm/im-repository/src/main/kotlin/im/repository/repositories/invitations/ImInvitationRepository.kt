package im.repository.repositories.invitations

import im.domain.invitations.ImInvitation
import im.repository.repositories.Repository
import java.util.UUID

/**
 * [Repository] for [ImInvitation] entities.
 */
interface ImInvitationRepository : Repository<ImInvitation, UUID> {
    /**
     * Deletes all expired invitations.
     */
    fun deleteExpired()
}
