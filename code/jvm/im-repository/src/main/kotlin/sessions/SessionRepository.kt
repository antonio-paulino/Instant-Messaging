package sessions

import Repository
import tokens.AccessToken
import tokens.RefreshToken

interface SessionRepository : Repository<Session, Long> {
    fun findByAccessToken(accessToken: AccessToken): Session?
    fun findByRefreshToken(refreshToken: RefreshToken): Session?
}