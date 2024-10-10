package im.repository.repositories.tokens

import im.repository.repositories.Repository
import im.sessions.Session
import im.tokens.RefreshToken
import java.util.UUID

/**
 * [Repository] for [RefreshToken] entities.
 */
interface RefreshTokenRepository : Repository<RefreshToken, UUID> {
    /**
     * Finds all refresh tokens for a session.
     *
     * @param session the session
     * @return the refresh tokens for the session
     */
    fun findBySession(session: Session): List<RefreshToken>
}