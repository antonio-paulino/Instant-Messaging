package im.services.auth

sealed class AuthError {
    data object SessionLimitReached : AuthError()

    data object InvalidInvitationCode : AuthError()

    data object InvitationAlreadyUsed : AuthError()

    data object InvitationExpired : AuthError()

    data object InvalidCredentials : AuthError()

    data class UserAlreadyExists(
        val conflict: String,
    ) : AuthError()

    data object InvalidToken : AuthError()

    data object TokenExpired : AuthError()

    data object SessionExpired : AuthError()
}
