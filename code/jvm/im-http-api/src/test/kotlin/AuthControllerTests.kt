package im

import im.api.model.input.body.AuthenticationInputModel
import im.api.model.input.body.ImInvitationCreationInputModel
import im.api.model.input.body.UserCreationInputModel
import im.api.model.output.credentials.CredentialsOutputModel
import im.api.model.problems.ProblemOutputModel
import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.domain.wrappers.Name
import im.domain.wrappers.toPassword
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
abstract class AuthControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Autowired
    private lateinit var transactionManager: TransactionManager

    @BeforeEach
    fun setup() {
        cleanup(transactionManager)
        setUp()
    }

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

    private val testUser =
        User(
            0L,
            "testUser",
            "Password123",
            "iseldaw@isel.pt",
        )

    private val expiredSession =
        Session(
            0L,
            testUser,
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

    private val validSession =
        Session(
            0L,
            testUser,
            LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val validAccessToken =
        AccessToken(
            UUID.randomUUID(),
            validSession,
            LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
        )

    private val validRefreshToken =
        RefreshToken(
            UUID.randomUUID(),
            validSession,
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
            val user =
                userRepository.save(
                    testUser.copy(
                        password = "Q3M711-7mLKn-rPIBd6XdA==:Nm5pSZjtJG-S2xhw_xavPKJ1EOKNiG5w8NyuA6gGrZw=".toPassword(),
                    ),
                )
            val session = sessionRepository.save(expiredSession.copy(user = user))
            accessTokenRepository.save(expiredAccessToken1.copy(session = session))
            refreshTokenRepository.save(expiredRefreshToken1.copy(session = session))
            val session2 = sessionRepository.save(validSession.copy(user = user))
            accessTokenRepository.save(validAccessToken.copy(session = session2))
            refreshTokenRepository.save(validRefreshToken.copy(session = session2))
        }
    }

    @Test
    fun `register should create user and return tokens with 201`() {
        val credentials =
            UserCreationInputModel(
                "testuser2",
                testUser.password.value,
                "testemail@isel.pt",
                invitationCode1.token.toString(),
            )

        val client = getClient()

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectHeader()
            .valueMatches("Location", "/api/users/\\d+")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                val res = it.responseBody!!
                assertNotNull(res.accessToken)
                assertNotNull(res.refreshToken)
            }
    }

    @Test
    fun `register invalid invite should return 400`() {
        val client = getClient()

        val credentials =
            UserCreationInputModel(
                testUser.name.value,
                testUser.password.value,
                testUser.email.value,
                UUID.randomUUID().toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `register existing username should return 409`() {
        val credentials =
            UserCreationInputModel(
                testUser.name.value,
                testUser.password.value,
                testUser.email.value,
                invitationCode1.token.toString(),
            )

        val client = getClient()

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(409, problem.status)
                assertEquals("user-already-exists", problem.title)
            }
    }

    @Test
    fun `register with used invitation should return 400`() {
        transactionManager.run {
            imInvitationRepository.save(invitationCode1.use())
        }

        val client = getClient()

        val credentials =
            UserCreationInputModel(
                testUser.name.value,
                testUser.password.value,
                testUser.email.value,
                invitationCode1.token.toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `register with expired invitation should return 400`() {
        val client = getClient()

        val credentials =
            UserCreationInputModel(
                testUser.name.value,
                testUser.password.value,
                testUser.email.value,
                expiredInvitationCode.token.toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `register with existing email should return 409`() {
        val user: User = testUser.copy(name = Name("username2"))

        val client = getClient()

        val credentials =
            UserCreationInputModel(
                user.name.value,
                user.password.value,
                user.email.value,
                invitationCode1.token.toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(409, problem.status)
                assertEquals("user-already-exists", problem.title)
            }
    }

    @Test
    fun `login with valid username and password should return 200`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(
                mapOf(
                    "username" to testUser.name.value,
                    "password" to testUser.password.value,
                ),
            ).exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                val tokens = it.responseBody!!
                assertNotNull(tokens.accessToken)
                assertNotNull(tokens.refreshToken)
            }
    }

    @Test
    fun `login with valid email and password should return tokens 200`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(
                mapOf(
                    "email" to testUser.email.value,
                    "password" to testUser.password.value,
                ),
            ).exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                val credentials = it.responseBody!!
                assertNotNull(credentials.accessToken)
                assertNotNull(credentials.refreshToken)
            }
    }

    @Test
    fun `login with invalid username should return 401`() {
        val client = getClient()

        val credentials =
            AuthenticationInputModel(
                "wrongusername",
                testUser.password.value,
                null,
            )

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isUnauthorized
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(401, problem.status)
                assertEquals("unauthorized", problem.title)
            }
    }

    @Test
    fun `login with invalid email should return error`() {
        val client = getClient()

        val credentials =
            AuthenticationInputModel(
                null,
                testUser.password.value,
                "wrongemail@isel.pt",
            )

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isUnauthorized
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(401, problem.status)
                assertEquals("unauthorized", problem.title)
            }
    }

    @Test
    fun `login with invalid password should return error`() {
        val client = getClient()

        val credentials =
            AuthenticationInputModel(
                testUser.name.value,
                "wrongpassword",
                null,
            )

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(credentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }

    @Test
    fun `login and register should create different sessions`() {
        val client = getClient()

        val credentials =
            AuthenticationInputModel(
                testUser.name.value,
                testUser.password.value,
                null,
            )

        var sessionId1: Long? = null
        var sessionId2: Long? = null

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(credentials)
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                val res = it.responseBody!!
                assertNotNull(res.accessToken)
                assertNotNull(res.refreshToken)
                sessionId1 = res.sessionID
            }

        val credentials2 =
            UserCreationInputModel(
                "testuser2",
                testUser.password.value,
                "testuser2@isel.pt",
                invitationCode1.token.toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials2)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                val res = it.responseBody!!
                assertNotNull(res.accessToken)
                assertNotNull(res.refreshToken)
                sessionId2 = res.sessionID
            }

        assertNotNull(sessionId1)
        assertNotNull(sessionId2)

        assertNotEquals(sessionId1, sessionId2)
    }

    @Test
    fun `refreshSession with valid cookie should return new tokens`() {
        val client = getClient()

        var credentials: CredentialsOutputModel? = null
        var credentials2: CredentialsOutputModel? = null

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(
                mapOf(
                    "username" to testUser.name.value,
                    "password" to testUser.password.value,
                ),
            ).exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                credentials = it.responseBody!!
            }

        client
            .post()
            .uri("/api/auth/refresh")
            .cookie("refresh_token", credentials!!.refreshToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectCookie()
            .exists("access_token")
            .expectCookie()
            .exists("refresh_token")
            .expectBody(CredentialsOutputModel::class.java)
            .consumeWith {
                credentials2 = it.responseBody!!
            }

        assertNotNull(credentials)
        assertNotNull(credentials2)
        assertNotEquals(credentials!!.accessToken, credentials2!!.accessToken)
        assertNotEquals(credentials!!.refreshToken, credentials2!!.refreshToken)
        assertEquals(credentials!!.sessionID, credentials2!!.sessionID)
    }

    @Test
    fun `refreshSession with invalid token should return error`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/refresh")
            .cookie("refresh_token", UUID.randomUUID().toString())
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .contentType("application/problem+json")
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(401, problem.status)
                assertEquals("unauthorized", problem.title)
            }
    }

    @Test
    fun `refreshSession with expired session should return error`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/refresh")
            .cookie("refresh_token", expiredRefreshToken1.token.toString())
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectHeader()
            .contentType("application/problem+json")
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(401, problem.status)
                assertEquals("unauthorized", problem.title)
            }
    }

    @Test
    fun `logout with valid token should succeed`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/logout")
            .headers {
                it.set("Authorization", "Bearer ${validAccessToken.token}")
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .isEmpty
    }

    @Test
    fun `logout with invalid token should return error`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/logout")
            .cookie("access_token", UUID.randomUUID().toString())
            .exchange()
            .expectStatus()
            .isUnauthorized
            .expectBody()
            .isEmpty
    }

    @Test
    fun `createInvitation with valid expiration should return invitation`() {
        val client = getClient()

        client
            .post()
            .uri("/api/auth/invitations")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .contentType("application/json")
            .expectBody(ImInvitation::class.java)
            .consumeWith {
                val invitation = it.responseBody!!
                assertNotNull(invitation)
                assertEquals(ImInvitationStatus.PENDING, invitation.status)
            }
    }

    @Test
    fun `createInvitation with too short expiration should return error`() {
        val client = getClient()

        val expiration =
            ImInvitationCreationInputModel(
                LocalDateTime.now().plusMinutes(1),
            )

        client
            .post()
            .uri("/api/auth/invitations")
            .bodyValue(expiration)
            .headers {
                it.set("Authorization", "Bearer ${validAccessToken.token}")
            }.exchange()
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType("application/problem+json")
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `createInvitation with too long expiration should return error`() {
        val client = getClient()

        val expiration =
            ImInvitationCreationInputModel(
                LocalDateTime.now().plusDays(31),
            )

        client
            .post()
            .uri("/api/auth/invitations")
            .bodyValue(expiration)
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType("application/problem+json")
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `invitation used twice should fail`() {
        val client = getClient()

        val credentials =
            UserCreationInputModel(
                "testuser2",
                testUser.password.value,
                "testemail2@isel.pt",
                invitationCode1.token.toString(),
            )

        val credentials2 =
            UserCreationInputModel(
                "testuser3",
                testUser.password.value,
                "testemail3@isel.pt",
                invitationCode1.token.toString(),
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(credentials2)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType("application/problem+json")
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-invitation", problem.title)
            }
    }

    @Test
    fun `try to login with invalid username input`() {
        val client = getClient()

        val invalidCredentials =
            mapOf(
                "username" to "",
                "password" to "test",
            )

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }

    @Test
    fun `try to login with invalid password input`() {
        val client = getClient()

        val invalidCredentials =
            mapOf(
                "username" to "test",
                "password" to "",
            )

        client
            .post()
            .uri("/api/auth/login")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }

    @Test
    fun `try to register with invalid username input`() {
        val client = getClient()

        val invalidCredentials =
            UserCreationInputModel(
                "",
                "",
                "",
                "",
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }

    @Test
    fun `try to register with invalid password input`() {
        val client = getClient()

        val invalidCredentials =
            UserCreationInputModel(
                "",
                "",
                "",
                "",
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }

    @Test
    fun `try to register with invalid email input`() {
        val client = getClient()

        val invalidCredentials =
            UserCreationInputModel(
                "",
                "",
                "",
                "",
            )

        client
            .post()
            .uri("/api/auth/register")
            .bodyValue(invalidCredentials)
            .exchange()
            .expectHeader()
            .contentType("application/problem+json")
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .consumeWith {
                val problem = it.responseBody!!
                assertEquals(400, problem.status)
                assertEquals("invalid-input", problem.title)
            }
    }
}
