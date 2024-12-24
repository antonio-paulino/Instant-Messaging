package im.domain.user

import java.util.UUID

data class AuthenticatedUser(
    val user: User,
    val usedAccessToken: UUID,
)
