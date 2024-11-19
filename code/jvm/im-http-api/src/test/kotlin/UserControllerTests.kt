package im

import im.api.model.output.channel.ChannelsPaginatedOutputModel
import im.api.model.output.users.UserOutputModel
import im.api.model.output.users.UsersPaginatedOutputModel
import im.api.model.problems.InputValidationProblemOutputModel
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.user.User
import im.domain.wrappers.identifier.toIdentifier
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Profile
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Profile("!rateLimit")
abstract class UserControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser1: User = User(1L, "testUser1", "testPassword1", "test@daw.isel.pt")
    private var testUser2: User = User(2L, "testUser2", "testPassword2", "test2@daw.isel.pt")

    private var session = Session(1L, testUser1, LocalDateTime.now().plusMonths(1))

    private val validAccessToken =
        AccessToken(
            UUID.randomUUID(),
            session,
            LocalDateTime.now().plusHours(1),
        )

    private var testChannel1: Channel =
        Channel(
            1L,
            "testChannel1",
            ChannelRole.MEMBER,
            owner = testUser1,
            true,
            members = mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER),
        )
    private var testChannel2: Channel =
        Channel(
            2L,
            "testChannel2",
            ChannelRole.MEMBER,
            owner = testUser2,
            true,
            members = mapOf(testUser2 to ChannelRole.OWNER),
        )

    @BeforeEach
    fun setup() {
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
        transactionManager.run {
            testUser1 = userRepository.save(testUser1)
            testUser2 =
                userRepository.save(testUser2.copy(id = (testUser1.id.value + 1).toIdentifier())) // Avoid id collision
            testChannel1 =
                testChannel1.copy(
                    owner = testUser1,
                    membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) },
                )
            testChannel2 =
                testChannel2.copy(
                    owner = testUser2,
                    membersLazy = lazy { mapOf(testUser2 to ChannelRole.OWNER, testUser2 to ChannelRole.OWNER) },
                )
            testChannel1 = channelRepository.save(testChannel1)
            testChannel2 = channelRepository.save(testChannel2)
            session = sessionRepository.save(session.copy(user = testUser1))
            accessTokenRepository.save(validAccessToken.copy(session = session))
        }
    }

    @Test
    fun `get user by id no auth`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users/${testUser1.id}")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get user by id should return user`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users/${testUser1.id}")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UserOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals(testUser1.id.value, it!!.id)
                assertEquals(testUser1.name.value, it.name)
                assertEquals(testUser1.email.value, it.email)
            }
    }

    @Test
    fun `get user by id user not found`() {
        val client = getClient()
        client
            .get()
            .uri("/api/users/0")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `get user by id invalid id`() {
        val client = getClient()
        client
            .get()
            .uri("/api/users/invalid")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(InputValidationProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals("invalid-input", it!!.title)
            }
    }

    @Test
    fun `get users no auth`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get users should return users`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertEquals(2, users.size)
                assertEquals(testUser1.id.value, users[0].id)
                assertEquals(testUser2.id.value, users[1].id)
                assertEquals(1, info.current)
                assertEquals(1, info.totalPages)
                assertEquals(2, info.total)
                assertEquals(null, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get users should return users with name`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?name=test")
            .headers {
                it.setBearerAuth(validAccessToken.token.toString())
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertEquals(2, users.size)
                assertEquals(testUser1.id.value, users[0].id)
                assertEquals(testUser2.id.value, users[1].id)
                assertNotNull(info)
                assertEquals(1, info.current)
                assertEquals(1, info.totalPages)
                assertEquals(2, info.total)
                assertEquals(null, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get users non existing name should return empty`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?name=nonExisting")
            .headers {
                it.setBearerAuth(validAccessToken.token.toString())
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertNotNull(info)
                assertEquals(0, users.size)
                assertEquals(1, info.current)
                assertEquals(0, info.totalPages)
                assertEquals(0, info.total)
                assertEquals(null, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get users paginated should return users`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?offset=0&limit=1")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertEquals(1, users.size)
                assertEquals(testUser1.id.value, users[0].id)
                assertNotNull(info)
                assertEquals(1, info.current)
                assertEquals(2, info.totalPages)
                assertEquals(2, info.total)
                assertEquals(2, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get users last page should return users`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?offset=1&limit=1")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertEquals(1, users.size)
                assertEquals(testUser2.id.value, users[0].id)
                assertNotNull(info)
                assertEquals(2, info.current)
                assertEquals(2, info.totalPages)
                assertEquals(2, info.total)
                assertEquals(null, info.next)
                assertEquals(1, info.previous)
            }
    }

    @Test
    fun `get users paginated no count 200`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?offset=0&limit=1&getCount=false")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(UsersPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val users = it!!.users
                val info = it.pagination
                assertEquals(1, users.size)
                assertEquals(testUser1.id.value, users[0].id)
                assertNotNull(info)
                assertEquals(1, info.current)
                assertEquals(null, info.totalPages)
                assertEquals(null, info.total)
                assertEquals(2, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get user channels should return user channels`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users/${testUser1.id}/channels")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val channels = it!!.channels
                assertEquals(1, channels.size)
                assertEquals(testChannel1.id.value, channels[0].id)
                assertEquals(testChannel1.name.value, channels[0].name)
            }
    }

    @Test
    fun `get user channels should return 2 channels`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.addMember(testChannel2, testUser1, ChannelRole.MEMBER)
        }

        client
            .get()
            .uri("/api/users/${testUser1.id}/channels")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val channels = it!!.channels
                assertEquals(2, channels.size)
                assertTrue(channels.any { it.id == testChannel1.id.value })
                assertTrue(channels.any { it.id == testChannel2.id.value })
            }
    }

    @Test
    fun `get user channels 2 channels first page`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.addMember(testChannel2, testUser1, ChannelRole.MEMBER)
        }

        client
            .get()
            .uri("/api/users/${testUser1.id}/channels?offset=0&limit=1&getCount=false")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val channels = it!!.channels
                val info = it.pagination
                assertEquals(1, channels.size)
                assertEquals(testChannel1.id.value, channels[0].id)
                assertEquals(1, info.current)
                assertEquals(null, info.totalPages)
                assertEquals(null, info.total)
                assertEquals(2, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get user channels filter owned should return 1 channel`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.addMember(testChannel2, testUser1, ChannelRole.MEMBER)
        }

        client
            .get()
            .uri("/api/users/${testUser1.id}/channels?filterOwned=true")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val channels = it!!.channels
                assertEquals(1, channels.size)
                assertEquals(testChannel1.id.value, channels[0].id)
                assertEquals(testChannel1.name.value, channels[0].name)
            }
    }

    @Test
    fun `get user channels filter owned should return empty`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.delete(testChannel1)
        }

        client
            .get()
            .uri("/api/users/${testUser1.id}/channels?filterOwned=false")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                val channels = it!!.channels
                val info = it.pagination
                assertEquals(0, channels.size)
                assertEquals(0, it.pagination.total)
                assertEquals(1, info.current)
                assertEquals(0, info.totalPages)
                assertEquals(null, info.next)
                assertEquals(null, info.previous)
            }
    }

    @Test
    fun `get user channels different user`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users/${testUser2.id}/channels")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `get user with invalid pagination`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?offset=-1&limit=0")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(InputValidationProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals("invalid-input", it!!.title)
            }
    }

    @Test
    fun `get user with invalid pagination2`() {
        val client = getClient()

        client
            .get()
            .uri("/api/users?offset=2&limit=iu")
            .cookie("access_token", validAccessToken.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(InputValidationProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals("invalid-input", it!!.title)
            }
    }
}
