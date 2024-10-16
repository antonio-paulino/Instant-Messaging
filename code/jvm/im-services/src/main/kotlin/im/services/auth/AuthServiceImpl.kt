package im.services.auth

import im.domain.Either
import im.domain.Failure
import im.domain.Success
import im.domain.failure
import im.domain.invitations.ImInvitationStatus
import im.domain.sessions.Session
import im.domain.success
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.domain.wrappers.email.Email
import im.domain.wrappers.name.Name
import im.domain.wrappers.password.Password
import im.repository.repositories.transactions.Transaction
import im.repository.repositories.transactions.TransactionManager
import jakarta.inject.Named
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Named
class AuthServiceImpl(
    private val transactionManager: TransactionManager,
    private val passwordEncoder: PasswordEncoderSHA256,
    private val config: AuthConfig,
) : AuthService {
    override fun register(
        username: Name,
        password: Password,
        email: Email,
        invitationCode: UUID,
    ): Either<AuthError, User> =
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

            success(user)
        }

    override fun login(
        username: Name?,
        password: Password,
        email: Email?,
    ): Either<AuthError, Pair<AccessToken, RefreshToken>> =
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

            val session = createSession(user)

            if (session is Failure) {
                return@run session
            }
            session as Success
            success(session.value)
        }

    override fun refreshSession(refreshToken: UUID): Either<AuthError, Pair<AccessToken, RefreshToken>> =
        transactionManager.run {
            val oldToken =
                refreshTokenRepository.findById(refreshToken)
                    ?: return@run failure(AuthError.InvalidToken)

            val session = oldToken.session

            if (session.expired) {
                return@run failure(AuthError.SessionExpired)
            }

            val newSession =
                session.refresh(LocalDateTime.now().plusMinutes(config.accessTokenTTL.inWholeMinutes))

            val newAccessToken =
                accessTokenRepository.save(
                    AccessToken(session = newSession, expiresAt = newSession.expiresAt),
                )

            val newRefreshToken =
                refreshTokenRepository.save(
                    RefreshToken(session = newSession),
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

    private fun Transaction.createSession(user: User): Either<AuthError, Pair<AccessToken, RefreshToken>> {
        val activeSessions = sessionRepository.findByUser(user).filter { !it.expired }

        if (activeSessions.size >= config.maxSessions) {
            return failure(AuthError.SessionLimitReached)
        }

        val session =
            sessionRepository.save(
                Session(
                    user = user,
                    expiresAt =
                        LocalDateTime
                            .now()
                            .plusMinutes(config.sessionTTL.inWholeMinutes)
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
                            .plusMinutes(config.accessTokenTTL.inWholeMinutes)
                            .truncatedTo(ChronoUnit.MILLIS),
                ),
            )

        val refreshToken =
            refreshTokenRepository.save(
                RefreshToken(session = session),
            )

        return success(accessToken to refreshToken)
    }
}
