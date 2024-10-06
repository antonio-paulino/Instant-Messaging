package im.model.input

import im.user.User
import java.util.UUID

data class AuthenticatedUser(
    val user: User,
    val usedAccessToken: UUID
)