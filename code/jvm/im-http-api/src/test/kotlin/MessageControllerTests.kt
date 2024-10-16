package im

import im.api.model.input.body.MessageCreationInputModel
import im.api.model.output.messages.MessageCreationOutputModel
import im.api.model.output.messages.MessageOutputModel
import im.api.model.output.messages.MessageUpdateOutputModel
import im.api.model.output.messages.MessagesPaginatedOutputModel
import im.api.model.problems.ProblemOutputModel
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.messages.Message
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.user.User
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
abstract class MessageControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser = User(1L, "testUser", "testPassword1", "iseldaw@isel.pt")
    private var testUser2 = User(2L, "testUser2", "testPassword2", "iseldaw2@isel.pt")
    private var testUser3 = User(3L, "testUser3", "testPassword3", "iseldaw3@isel.pt")
    private var testChannel = Channel(1L, "testChannel", testUser, true)
    private var testChannel2 = Channel(2L, "testChannel2", testUser, true)

    private var session = Session(0L, testUser, LocalDateTime.now().plusDays(1))
    private var session2 = Session(0L, testUser2, LocalDateTime.now().plusDays(1))
    private var session3 = Session(0L, testUser3, LocalDateTime.now().plusDays(1))

    private var accessToken1 = AccessToken(UUID.randomUUID(), session, LocalDateTime.now().plusDays(1))
    private var accessToken2 = AccessToken(UUID.randomUUID(), session2, LocalDateTime.now().plusDays(1))
    private var accessToken3 = AccessToken(UUID.randomUUID(), session3, LocalDateTime.now().plusDays(1))

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
            testUser = userRepository.save(testUser)
            testUser2 = userRepository.save(testUser2)
            testUser3 = userRepository.save(testUser3)
            testChannel =
                testChannel.copy(
                    owner = testUser,
                    membersLazy =
                        lazy {
                            mapOf(
                                testUser to ChannelRole.OWNER,
                                testUser2 to ChannelRole.MEMBER,
                                testUser3 to ChannelRole.GUEST,
                            )
                        },
                )
            testChannel = channelRepository.save(testChannel)
            testChannel2 = testChannel2.copy(owner = testUser, membersLazy = lazy { mapOf(testUser to ChannelRole.OWNER) })
            testChannel2 = channelRepository.save(testChannel2)
            session = sessionRepository.save(session.copy(user = testUser))
            session2 = sessionRepository.save(session2.copy(user = testUser2))
            session3 = sessionRepository.save(session3.copy(user = testUser3))
            accessToken1 = accessTokenRepository.save(accessToken1.copy(session = session))
            accessToken2 = accessTokenRepository.save(accessToken2.copy(session = session2))
            accessToken3 = accessTokenRepository.save(accessToken3.copy(session = session3))
        }
    }

    @Test
    fun `test create message should return message id with 200`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .post()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .contentType("application/json")
            .expectHeader()
            .valueMatches("Location", "/api/channels/${testChannel.id}/messages/\\d+")
            .expectBody(MessageCreationOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertTrue(it!!.createdAt.isBefore(LocalDateTime.now()))
                assertNull(it.editedAt)
            }
    }

    @Test
    fun `create message no auth should return 401`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .post()
            .uri("api/channels/${testChannel.id}/messages")
            .bodyValue(message)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `create message channel not found should return 404`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .post()
            .uri("api/channels/0/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-not-found", it!!.title)
            }
    }

    @Test
    fun `create message guest cannot write should return failure`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .post()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken3.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `create message user not in channel should return 403`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .post()
            .uri("api/channels/${testChannel2.id}/messages")
            .headers {
                it.setBearerAuth(accessToken2.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `test get channel messages channel not found 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/0/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-not-found", it!!.title)
            }
    }

    @Test
    fun `test get channel messages user not in channel 403`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel2.id}/messages")
            .cookie("access_token", accessToken3.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-channel", it!!.title)
            }
    }

    @Test
    fun `test get channel messages no auth should return 401`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `test get channel messages should return empty with 200`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertTrue(it!!.messages.isEmpty())
            }
    }

    @Test
    fun `test get channel messages should return messages with 200`() {
        val client = getClient()

        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertTrue(it!!.messages.isNotEmpty())
                val msg = it.messages[0]
                val info = it.pagination
                assertEquals(createdMessage.id.value, msg.id)
                assertNull(msg.editedAt)
                assertEquals(createdMessage.content, msg.content)
                assertEquals(createdMessage.id.value, msg.id)
                assertEquals(testUser.id.value, msg.author.id)
                assertEquals(testUser.name.value, msg.author.name)
                assertNotNull(info)
                assertEquals(1, info!!.total)
                assertEquals(1, info.totalPages)
                assertEquals(1, info.current)
                assertNull(info.next)
                assertNull(info.previous)
            }
    }

    @Test
    fun `get messages paginated should return messages 200`() {
        val message1 =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message 1",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        transactionManager.run {
            messageRepository.save(
                Message(
                    1L,
                    testChannel,
                    testUser,
                    "test message 2",
                    LocalDateTime.now(),
                    null,
                ),
            )
        }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages?page=1&size=1")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.messages.size)
                val msg = it.messages[0]
                val info = it.pagination
                assertEquals(message1.id.value, msg.id)
                assertNull(msg.editedAt)
                assertEquals(message1.content, msg.content)
                assertEquals(message1.id.value, msg.id)
                assertEquals(testUser.id.value, msg.author.id)
                assertEquals(testUser.name.value, msg.author.name)
                assertNotNull(info)
                assertEquals(2, info!!.total)
                assertEquals(2, info.totalPages)
                assertEquals(1, info.current)
                assertEquals(2, info.next)
                assertNull(info.previous)
            }
    }

    @Test
    fun `get messages paginated desc sort should return messages`() {
        val message1 =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message 1",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }
        Thread.sleep(100) // ensure different timestamps
        val message2 =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        // avoid id collision, would update instead of insert
                        message1.id.value + 1,
                        testChannel,
                        testUser,
                        "test message 2",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages?page=1&size=1&sort=desc")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.messages.size)
                val msg = it.messages[0]
                val info = it.pagination
                assertEquals(message2.id.value, msg.id)
                assertNull(msg.editedAt)
                assertEquals(message2.content, msg.content)
                assertEquals(message2.id.value, msg.id)
                assertEquals(testUser.id.value, msg.author.id)
                assertEquals(testUser.name.value, msg.author.name)
                assertNotNull(info)
                assertEquals(2, info!!.total)
                assertEquals(2, info.totalPages)
                assertEquals(1, info.current)
                assertEquals(2, info.next)
                assertNull(info.previous)
            }
    }

    @Test
    fun `get messages paginated page two sort desc should return first message 200`() {
        val message1 =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        1L,
                        testChannel,
                        testUser,
                        "test message 1",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }
        Thread.sleep(100) // ensure different timestamps
        transactionManager.run {
            messageRepository.save(
                Message(
                    // avoid id collision, would update instead of insert
                    message1.id.value + 1,
                    testChannel,
                    testUser,
                    "test message 2",
                    LocalDateTime.now(),
                    null,
                ),
            )
        }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages?page=2&size=1&sort=desc")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.messages.size)
                val msg = it.messages[0]
                val info = it.pagination
                assertEquals(message1.id.value, msg.id)
                assertNull(msg.editedAt)
                assertEquals(message1.content, msg.content)
                assertEquals(message1.id.value, msg.id)
                assertEquals(testUser.id.value, msg.author.id)
                assertEquals(testUser.name.value, msg.author.name)
                assertEquals(2, info!!.total)
                assertEquals(2, info.totalPages)
                assertEquals(2, info.current)
                assertNull(info.next)
                assertEquals(1, info.previous)
            }
    }

    @Test
    fun `get messages paginated no count 200`() {
        val message1 =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message 1",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        transactionManager.run {
            messageRepository.save(
                Message(
                    1L,
                    testChannel,
                    testUser,
                    "test message 2",
                    LocalDateTime.now(),
                    null,
                ),
            )
        }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages?page=1&size=1&getCount=false")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MessagesPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.messages.size)
                val msg = it.messages[0]
                val info = it.pagination
                assertEquals(message1.id.value, msg.id)
                assertNull(msg.editedAt)
                assertEquals(message1.content, msg.content)
                assertEquals(message1.id.value, msg.id)
                assertEquals(testUser.id.value, msg.author.id)
                assertEquals(testUser.name.value, msg.author.name)
                assertNotNull(info)
                assertNull(info!!.total)
                assertNull(info.totalPages)
                assertEquals(1, info.current)
                assertEquals(2, info.next)
                assertNull(info.previous)
            }
    }

    @Test
    fun `update message no auth`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .put()
            .uri("api/channels/${testChannel.id}/messages/0")
            .bodyValue(message)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `update message message not found should return 404`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .put()
            .uri("api/channels/${testChannel.id}/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("message-not-found", it!!.title)
            }
    }

    @Test
    fun `update message channel not found should return failure 404`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        client
            .put()
            .uri("api/channels/0/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-not-found", it!!.title)
            }
    }

    @Test
    fun `update message cannot edit message should return failure with 403`() {
        val client = getClient()

        val message = MessageCreationInputModel("test message")

        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        client
            .put()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken2.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-update-message", it!!.title)
            }
    }

    @Test
    fun `update message should return updated message with 200`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        val message = MessageCreationInputModel("updated message")

        client
            .put()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(MessageUpdateOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertNotNull(it!!.editedAt)
            }
    }

    @Test
    fun `delete message no auth`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id}/messages/0")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `delete message message not found should return failure 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id}/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("message-not-found", it!!.title)
            }
    }

    @Test
    fun `delete message channel not found should return failure with 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/0/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-not-found", it!!.title)
            }
    }

    @Test
    fun `delete message cannot delete message should return failure with 403`() {
        val client = getClient()

        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        client
            .delete()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken2.token.toString())
            }.exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-delete-message", it!!.title)
            }
    }

    @Test
    fun `delete message should delete message 204`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `delete message channel owner can delete message returns 204`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser2,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `find message by id no auth`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages/0")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `find message by id should return message`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody(MessageOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(createdMessage.id.value, it!!.id)
                assertTrue(it.createdAt.isBefore(LocalDateTime.now()))
                assertNull(it.editedAt)
                assertEquals(createdMessage.content, it.content)
            }
    }

    @Test
    fun `find message by id message not found should return failure 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("message-not-found", it!!.title)
            }
    }

    @Test
    fun `find message by id channel not found should return failure 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/0/messages/0")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-not-found", it!!.title)
            }
    }

    @Test
    fun `find message by id user not in channel should return failure`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel2.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken2.token.toString())
            }.exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-channel", it!!.title)
            }
    }

    @Test
    fun `find message by id message in different channel 404`() {
        val createdMessage =
            transactionManager.run {
                messageRepository.save(
                    Message(
                        0L,
                        testChannel2,
                        testUser,
                        "test message",
                        LocalDateTime.now(),
                        null,
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id}/messages/${createdMessage.id.value}")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("message-not-found", it!!.title)
            }
    }

    @Test
    fun `create message with empty content should return 400`() {
        val client = getClient()

        val message = MessageCreationInputModel("")

        client
            .post()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-input", it!!.title)
            }
    }

    @Test
    fun `create message with content exceeding max length should return 400`() {
        val client = getClient()

        val longContent = "a".repeat(301)
        val message = MessageCreationInputModel(longContent)

        client
            .post()
            .uri("api/channels/${testChannel.id}/messages")
            .headers {
                it.setBearerAuth(accessToken1.token.toString())
            }.bodyValue(message)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-input", it!!.title)
            }
    }
}
