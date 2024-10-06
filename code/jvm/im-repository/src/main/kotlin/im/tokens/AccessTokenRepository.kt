package im.tokens

import im.Repository
import java.util.UUID

/**
 * [Repository] for [AccessToken] entities.
 */
interface AccessTokenRepository : Repository<AccessToken, UUID>