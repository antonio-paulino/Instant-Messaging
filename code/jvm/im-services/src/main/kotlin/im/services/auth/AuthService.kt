package im.services.auth

import im.domain.Either
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.domain.wrappers.email.Email
import im.domain.wrappers.name.Name
import im.domain.wrappers.password.Password
import java.util.UUID

/**
 * Service that handles authentication business logic.
 */
interface AuthService {
    /**
     * Registers a new user.
     *
     * - The username must be unique.
     * - The email must be unique.
     * - The invitation code must be valid.
     *
     * When a user is registered, the invitation code should be marked as used and no longer valid.
     *
     * @param username the username
     * @param password the password
     * @param email the email
     * @param invitationCode the invitation code
     * @return the user if the registration is successful, or an [AuthError] otherwise
     */
    fun register(
        username: Name,
        password: Password,
        email: Email,
        invitationCode: UUID,
    ): Either<AuthError, User>

    /**
     * Logs in a user.
     *
     * @param username the username
     * @param password the password
     * @param email the email
     * @return a pair of access and refresh tokens if the login is successful, or an [AuthError] otherwise
     */
    fun login(
        username: Name?,
        password: Password,
        email: Email?,
    ): Either<AuthError, Pair<AccessToken, RefreshToken>>

    /**
     * Refreshes the session. This method is used to extend the session expiration time,
     * and should generate a new access and refresh token pair.
     *
     * @param refreshToken the refresh token
     * @return a pair of access and refresh tokens if the session is refreshed, or an [AuthError] otherwise
     */
    fun refreshSession(refreshToken: UUID): Either<AuthError, Pair<AccessToken, RefreshToken>>

    /**
     * Authenticates a user.
     *
     * @param token the token
     * @return the user if the token is valid, or an [AuthError] otherwise
     */
    fun authenticate(token: UUID): Either<AuthError, User>

    /**
     * Logs out a user.
     *
     * This method should invalidate the session that the provided access token belongs to.
     *
     * @param token the token
     * @return [Unit] if the logout is successful, or an [AuthError] otherwise
     */
    fun logout(token: UUID): Either<AuthError, Unit>
}
