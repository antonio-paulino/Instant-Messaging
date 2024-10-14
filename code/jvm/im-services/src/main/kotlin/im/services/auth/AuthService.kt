package im.services.auth

import im.domain.invitations.ImInvitation
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.domain.wrappers.Email
import im.domain.wrappers.Name
import im.domain.wrappers.Password
import im.services.Either
import java.time.LocalDateTime
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
     * @return a pair of access and refresh tokens if the registration is successful, or an [AuthError] otherwise
     */
    fun register(
        username: Name,
        password: Password,
        email: Email,
        invitationCode: UUID,
    ): Either<AuthError, Pair<AccessToken, RefreshToken>>

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

    /**
     * Creates an invitation.
     *
     * @param expiration the expiration date of the invitation
     * @return the invitation if it is created, or an [AuthError] otherwise
     */
    fun createInvitation(expiration: LocalDateTime?): Either<AuthError, ImInvitation>
}
