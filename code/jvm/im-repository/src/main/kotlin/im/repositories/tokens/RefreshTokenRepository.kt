package im.repositories.tokens

import im.repositories.Repository
import im.tokens.RefreshToken
import java.util.UUID

/**
 * [Repository] for [RefreshToken] entities.
 */
interface RefreshTokenRepository : Repository<RefreshToken, UUID>