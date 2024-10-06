package im.sessions

import im.Repository
import im.tokens.AccessToken
import im.tokens.RefreshToken

/**
 * [Repository] for [Session] entities.
 */
interface SessionRepository : Repository<Session, Long> {
    /**
     * Finds all access tokens associated with a session.
     *
     * @param session the session
     * @return the access tokens associated with the session
     */
    fun getAccessTokens(session: Session): List<AccessToken>

    /**
     * Finds all refresh tokens associated with a session.
     *
     * @param session the session
     * @return the refresh tokens associated with the session
     */
    fun getRefreshTokens(session: Session): List<RefreshToken>
}