package im.repository.repositories.tokens

import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.repository.repositories.Repository
import java.util.UUID

/**
 * [Repository] for [AccessToken] entities.
 */
interface AccessTokenRepository : Repository<AccessToken, UUID> {
    /**
     * Finds all access tokens for a session.
     *
     * @param session the session
     * @return the access tokens for the session
     */
    fun findBySession(session: Session): List<AccessToken>

    /**
     * Deletes all expired access tokens.
     */
    fun deleteExpired()
}
