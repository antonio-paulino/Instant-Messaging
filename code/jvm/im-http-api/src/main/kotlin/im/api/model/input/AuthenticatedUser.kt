package im.api.model.input

import im.domain.user.User
import java.util.UUID

data class AuthenticatedUser(
    val user: User,
    val usedAccessToken: UUID,
)
