package im.repository.repositories.sessions

import im.repository.repositories.Repository
import im.sessions.Session
import im.user.User
import im.wrappers.Identifier

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
}