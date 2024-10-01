package tokens

import Repository
import java.util.UUID

interface RefreshTokenRepository : Repository<RefreshToken, UUID>