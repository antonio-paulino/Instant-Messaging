package im.repositories.tokens

import im.repositories.Repository
import im.tokens.AccessToken
import java.util.UUID

/**
 * [Repository] for [AccessToken] entities.
 */
interface AccessTokenRepository : Repository<AccessToken, UUID>