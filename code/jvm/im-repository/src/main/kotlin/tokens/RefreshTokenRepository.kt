package tokens

import Repository
import java.util.UUID

/**
 * [Repository] for [RefreshToken] entities.
 */
interface RefreshTokenRepository : Repository<RefreshToken, UUID>