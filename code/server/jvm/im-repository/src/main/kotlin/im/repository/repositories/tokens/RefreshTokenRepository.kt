package im.repository.repositories.tokens

import im.domain.sessions.Session
import im.domain.tokens.RefreshToken
import im.repository.repositories.Repository
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
