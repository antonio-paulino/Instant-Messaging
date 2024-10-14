package im.repository.repositories.sessions

import im.domain.sessions.Session
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.repositories.Repository

/**
 * [Repository] for [Session] entities.
 */
interface SessionRepository : Repository<Session, Identifier> {
    /**
     * Finds all sessions for a user.
     *
     * @param user the user
     * @return the sessions for the user
     */
    fun findByUser(user: User): List<Session>

    /**
     * Deletes all expired sessions.
     */
    fun deleteExpired()
}
