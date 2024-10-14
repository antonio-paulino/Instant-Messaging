package im

import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.domain.wrappers.Email
import im.domain.wrappers.Name
import im.domain.wrappers.Password
import im.domain.wrappers.toEmail
import im.domain.wrappers.toName
import im.repository.repositories.transactions.TransactionManager
import im.services.Failure
import im.services.Success
import im.services.auth.AuthError
import im.services.auth.AuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertIs

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class AuthServiceTest {
    @Autowired
    private lateinit var transactionManager: TransactionManager

    @Autowired
    private lateinit var authService: AuthService

    private val invitationCode1 =
        ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val invitationCode2 =
        ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val expiredInvitationCode =
        ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val sessionUser =
        User(
            0L,
            "sessionUser",
            "password",
            "iseldawsession@isel.pt",
        )

    private val expiredSession =
        Session(
            0L,
            sessionUser,
            LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val expiredRefreshToken1 =
        RefreshToken(
            UUID.randomUUID(),
            expiredSession,
        )

    private val expiredAccessToken1 =
        AccessToken(
            UUID.randomUUID(),
            expiredSession,
            LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    @BeforeEach
    fun setUp() {
        cleanup(transactionManager)
        insertInvitationCode(transactionManager)
    }

    private fun cleanup(transactionManager: TransactionManager) {
        transactionManager.run {
            refreshTokenRepository.deleteAll()
            accessTokenRepository.deleteAll()
            imInvitationRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            messageRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            channelRepository.deleteAll()
            sessionRepository.deleteAll()
            userRepository.deleteAll()
        }
    }

    private fun insertInvitationCode(transactionManager: TransactionManager) {
        transactionManager.run {
            imInvitationRepository.save(invitationCode1)
            imInvitationRepository.save(invitationCode2)
            imInvitationRepository.save(expiredInvitationCode)
            val user = userRepository.save(sessionUser)
            val session = sessionRepository.save(expiredSession.copy(user = user))
            accessTokenRepository.save(expiredAccessToken1.copy(session = session))
            refreshTokenRepository.save(expiredRefreshToken1.copy(session = session))
        }
    }

    @Test
    fun `register should create user and return tokens`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val result = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(result)
        val (accessToken, refreshToken) = result.value
        assertNotNull(accessToken)
        assertNotNull(refreshToken)
    }

    @Test
    fun `register invalid invite should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = UUID.randomUUID()
        val result = authService.register(username, password, email, invitationCode)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidInvitationCode>(error)
    }

    @Test
    fun `register existing username should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val email2 = Email("testdaw2@isel.pt")

        authService.register(username, password, email, invitationCode1.token)
        val result = authService.register(username, password, email2, invitationCode2.token)

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.UserAlreadyExists>(error)
    }

    @Test
    fun `register with used invitation should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val email2 = Email("testdaw2@isel.pt")

        authService.register(username, password, email, invitationCode1.token)
        val result = authService.register(username, password, email2, invitationCode1.token)

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvitationAlreadyUsed>(error)
    }

    @Test
    fun `register with expired invitation should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")

        val result = authService.register(username, password, email, expiredInvitationCode.token)

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvitationExpired>(error)
    }

    @Test
    fun `register with existing email should return error`() {
        val username = Name("username")
        val username2 = Name("username2")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")

        authService.register(username, password, email, invitationCode1.token)
        val result = authService.register(username2, password, email, invitationCode2.token)

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.UserAlreadyExists>(error)
    }

    @Test
    fun `login with valid username and password should return tokens`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        authService.register(username, password, email, invitationCode)

        val result = authService.login(username, password, null)

        assertIs<Success<Pair<AccessToken, RefreshToken>>>(result)
        val (accessToken, refreshToken) = result.value
        assertNotNull(accessToken)
        assertNotNull(refreshToken)
    }

    @Test
    fun `login with valid email and password should return tokens`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        authService.register(username, password, email, invitationCode)

        val result = authService.login(null, password, email)

        assertIs<Success<Pair<AccessToken, RefreshToken>>>(result)
        val (accessToken, refreshToken) = result.value
        assertNotNull(accessToken)
        assertNotNull(refreshToken)
    }

    @Test
    fun `login with invalid username should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        authService.register(username, password, email, invitationCode)

        val result = authService.login("wrongusername".toName(), password, null)

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidCredentials>(error)
    }

    @Test
    fun `login with invalid email should return error`() {
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        authService.register(Name("username"), password, email, invitationCode)

        val result = authService.login(null, password, "wrong@isel.pt".toEmail())

        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidCredentials>(error)
    }

    @Test
    fun `login with invalid password should return error`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token

        authService.register(username, password, email, invitationCode)

        val result = authService.login(username, Password("wrongpassword"), null)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidCredentials>(error)
    }

    @Test
    fun `login and register should create different sessions`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val session1 = authService.register(username, password, email, invitationCode)

        val session2 = authService.login(username, password, null)

        val session3 = authService.login(null, password, email)

        assertIs<Success<Pair<AccessToken, RefreshToken>>>(session1)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(session2)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(session3)

        val session1Value = session1.value.first.session
        val session2Value = session2.value.first.session
        val session3Value = session3.value.first.session

        assertNotEquals(session1Value, session2Value)
        assertNotEquals(session1Value, session3Value)
        assertNotEquals(session2Value, session3Value)
    }

    @Test
    fun `refreshSession with valid token should return new tokens`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val registerResult = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(registerResult)
        val (accessToken, refreshToken) = registerResult.value

        val result = authService.refreshSession(refreshToken.token)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(result)
        val (newAccessToken, newRefreshToken) = result.value
        assertNotNull(newAccessToken)
        assertNotNull(newRefreshToken)

        assertNotEquals(accessToken, newAccessToken)
        assertNotEquals(refreshToken, newRefreshToken)

        assertEquals(accessToken.session.id, newAccessToken.session.id)
        assertEquals(refreshToken.session.id, newRefreshToken.session.id)
    }

    @Test
    fun `refreshSession with invalid token should return error`() {
        val invalidToken = UUID.randomUUID()
        val result = authService.refreshSession(invalidToken)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidToken>(error)
    }

    @Test
    fun `refreshSession with expired session should return error`() {
        val expiredRefreshToken = expiredRefreshToken1.token
        val result = authService.refreshSession(expiredRefreshToken)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.SessionExpired>(error)
    }

    @Test
    fun `authenticate with valid token should return user`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val registerResult = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(registerResult)
        val (accessToken, _) = registerResult.value

        val result = authService.authenticate(accessToken.token)
        assertIs<Success<User>>(result)
        val user = result.value
        assertEquals(username, user.name)
        assertEquals(email, user.email)
    }

    @Test
    fun `authenticate with expired token should return error`() {
        val expiredAccessToken = expiredAccessToken1.token
        val result = authService.authenticate(expiredAccessToken)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.TokenExpired>(error)
    }

    @Test
    fun `authenticate with invalid token should return error`() {
        val invalidToken = UUID.randomUUID()
        val result = authService.authenticate(invalidToken)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidToken>(error)
    }

    @Test
    fun `authenticate after log out should fail`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val registerResult = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(registerResult)

        val auth1 = authService.authenticate(registerResult.value.first.token)
        assertIs<Success<User>>(auth1)

        val (accessToken, _) = registerResult.value
        val logoutResult = authService.logout(accessToken.token)

        assertIs<Success<Unit>>(logoutResult)
        val authenticateResult = authService.authenticate(accessToken.token)

        assertIs<Failure<AuthError>>(authenticateResult)
        val error = authenticateResult.value
        assertIs<AuthError.InvalidToken>(error)
    }

    @Test
    fun `logout with valid token should succeed`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val registerResult = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(registerResult)
        val (accessToken, _) = registerResult.value

        val result = authService.logout(accessToken.token)
        assertIs<Success<Unit>>(result)
    }

    @Test
    fun `logout with invalid token should return error`() {
        val invalidToken = UUID.randomUUID()
        val result = authService.logout(invalidToken)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidToken>(error)
    }

    @Test
    fun `createInvitation with valid expiration should return invitation`() {
        val expiration = LocalDateTime.now().plusDays(1)
        val result = authService.createInvitation(expiration)
        assertIs<Success<ImInvitation>>(result)
        val invitation = result.value
        assertEquals(expiration, invitation.expiresAt)
    }

    @Test
    fun `createInvitation with too short expiration should return error`() {
        val expiration = LocalDateTime.now().plusMinutes(1)
        val result = authService.createInvitation(expiration)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidInvitationExpiration>(error)
    }

    @Test
    fun `createInvitation with too long expiration should return error`() {
        val expiration = LocalDateTime.now().plusDays(10)
        val result = authService.createInvitation(expiration)
        assertIs<Failure<AuthError>>(result)
        val error = result.value
        assertIs<AuthError.InvalidInvitationExpiration>(error)
    }

    @Test
    fun `create invitation, try to register 2 users with the same one, and then log out`() {
        val username = Name("username")
        val password = Password("password")
        val email = Email("testdaw@isel.pt")
        val invitationCode = invitationCode1.token
        val session1 = authService.register(username, password, email, invitationCode)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(session1)

        val expiration = LocalDateTime.now().plusDays(1)
        val invitation = authService.createInvitation(expiration)
        assertIs<Success<ImInvitation>>(invitation)

        val invitationCode2 = invitation.value.token
        val username2 = Name("username2")
        val password2 = Password("password2")
        val email2 = Email("testdaw2@isel.pt")
        val session2 = authService.register(username2, password2, email2, invitationCode2)
        assertIs<Success<Pair<AccessToken, RefreshToken>>>(session2)

        val username3 = Name("username3")
        val password3 = Password("password3")
        val email3 = Email("testdaw3@isel.pt")
        val session3 = authService.register(username3, password3, email3, invitationCode2)
        assertIs<Failure<AuthError>>(session3)
        val error = session3.value
        assertIs<AuthError.InvitationAlreadyUsed>(error)

        val res = authService.logout(session1.value.first.token)
        assertIs<Success<Unit>>(res)

        val res2 = authService.logout(session2.value.first.token)
        assertIs<Success<Unit>>(res2)
    }
}
