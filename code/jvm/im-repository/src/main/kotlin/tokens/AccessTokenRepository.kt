package tokens

import Repository
import java.util.UUID

/**
 * [Repository] for [AccessToken] entities.
 */
interface AccessTokenRepository : Repository<AccessToken, UUID>