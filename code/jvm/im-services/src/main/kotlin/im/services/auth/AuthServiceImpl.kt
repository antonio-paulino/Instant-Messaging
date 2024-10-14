package im.services.auth

import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.user.User
import im.domain.wrappers.Email
import im.domain.wrappers.Name
import im.domain.wrappers.Password
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.transactions.TransactionManager
import im.services.Either
import im.services.failure
import im.services.success
import jakarta.inject.Named
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Named
class AuthServiceImpl(
    private val transactionManager: TransactionManager,
    private val passwordEncoder: PasswordEncoder,
) : AuthService {
    companion object {
        private const val ACCESS_TOKEN_EXPIRATION_DAYS = 1L
        private const val SESSION_EXPIRATION_DAYS = 30L

        private const val MIN_INVITATION_EXPIRATION_MINUTES = 30L
        private const val DEFAULT_INVITATION_EXPIRATION_DAYS = 1L
        private const val MAX_INVITATION_EXPIRATION_DAYS = 7L
    }

    override fun register(
        username: Name,
        password: Password,
        email: Email,
        invitationCode: UUID,
    ): Either<AuthError, Pair<AccessToken, im.domain.tokens.RefreshToken>> =
        transactionManager.run {
            val imInvitation =
                imInvitationRepository.findById(invitationCode)
                    ?: return@run failure(AuthError.InvalidInvitationCode)

            if (imInvitation.status == ImInvitationStatus.USED) {
                return@run failure(AuthError.InvitationAlreadyUsed)
            }

            if (imInvitation.expired) {
                return@run failure(AuthError.InvitationExpired)
            }

            if (userRepository.findByName(username) != null) {
                return@run failure(AuthError.UserAlreadyExists("username"))
            }

            if (userRepository.findByEmail(email) != null) {
                return@run failure(AuthError.UserAlreadyExists("email"))
            }

            val user =
                userRepository.save(
                    User(name = username, password = passwordEncoder.encode(password), email = email),
                )

            val usedInvitation = imInvitation.use()

            imInvitationRepository.save(usedInvitation)

            val (accessToken, refreshToken) = createSession(user)

            success(accessToken to refreshToken)
        }

    override fun login(
        username: Name?,
        password: Password,
        email: Email?,
    ): Either<AuthError, Pair<AccessToken, im.domain.tokens.RefreshToken>> =
        transactionManager.run {
            val user =
                when {
                    username != null -> userRepository.findByName(username)
                    email != null -> userRepository.findByEmail(email)
                    else -> null
                }

            if (user == null) {
                return@run failure(AuthError.InvalidCredentials)
            }

            if (!passwordEncoder.verify(password, user.password)) {
                return@run failure(AuthError.InvalidCredentials)
            }

            val (accessToken, refreshToken) = createSession(user)

            success(accessToken to refreshToken)
        }

    override fun refreshSession(refreshToken: UUID): Either<AuthError, Pair<AccessToken, im.domain.tokens.RefreshToken>> =
        transactionManager.run {
            val oldToken =
                refreshTokenRepository.findById(refreshToken)
                    ?: return@run failure(AuthError.InvalidToken)

            val session = oldToken.session

            if (session.expired) {
                return@run failure(AuthError.SessionExpired)
            }

            val newSession =
                session.refresh(LocalDateTime.now().plusDays(SESSION_EXPIRATION_DAYS).truncatedTo(ChronoUnit.MILLIS))

            val newAccessToken =
                accessTokenRepository.save(
                    AccessToken(session = newSession, expiresAt = newSession.expiresAt),
                )

            val newRefreshToken =
                refreshTokenRepository.save(
                    im.domain.tokens.RefreshToken(session = newSession),
                )

            refreshTokenRepository.delete(oldToken)

            success(newAccessToken to newRefreshToken)
        }

    override fun authenticate(token: UUID): Either<AuthError, User> =
        transactionManager.run {
            val accessToken =
                accessTokenRepository.findById(token)
                    ?: return@run failure(AuthError.InvalidToken)

            if (accessToken.expired) {
                return@run failure(AuthError.TokenExpired)
            }

            success(accessToken.session.user)
        }

    override fun logout(token: UUID): Either<AuthError, Unit> =
        transactionManager.run {
            val accessToken =
                accessTokenRepository.findById(token)
                    ?: return@run failure(AuthError.InvalidToken)

            sessionRepository.delete(accessToken.session)

            success(Unit)
        }

    override fun createInvitation(expiration: LocalDateTime?): Either<AuthError, ImInvitation> =
        transactionManager.run {
            val expires = expiration ?: LocalDateTime.now().plusDays(DEFAULT_INVITATION_EXPIRATION_DAYS)

            if (expires.isBefore(LocalDateTime.now().plusMinutes(MIN_INVITATION_EXPIRATION_MINUTES))) {
                return@run failure(
                    AuthError.InvalidInvitationExpiration(
                        "Minimum expiration time is $MIN_INVITATION_EXPIRATION_MINUTES minutes",
                    ),
                )
            }

            if (expires.isAfter(LocalDateTime.now().plusDays(MAX_INVITATION_EXPIRATION_DAYS))) {
                return@run failure(
                    AuthError.InvalidInvitationExpiration(
                        "Maximum expiration time is $MAX_INVITATION_EXPIRATION_DAYS days",
                    ),
                )
            }

            val invitation =
                imInvitationRepository.save(
                    ImInvitation(expiresAt = expires),
                )

            success(invitation)
        }

    private fun Transaction.createSession(user: User): Pair<AccessToken, im.domain.tokens.RefreshToken> {
        val session =
            sessionRepository.save(
                Session(
                    user = user,
                    expiresAt =
                        LocalDateTime
                            .now()
                            .plusDays(SESSION_EXPIRATION_DAYS)
                            .truncatedTo(ChronoUnit.MILLIS),
                ),
            )

        val accessToken =
            accessTokenRepository.save(
                AccessToken(
                    session = session,
                    expiresAt =
                        LocalDateTime
                            .now()
                            .plusDays(ACCESS_TOKEN_EXPIRATION_DAYS)
                            .truncatedTo(ChronoUnit.MILLIS),
                ),
            )

        val refreshToken =
            refreshTokenRepository.save(
                im.domain.tokens.RefreshToken(session = session),
            )

        return accessToken to refreshToken
    }
}
