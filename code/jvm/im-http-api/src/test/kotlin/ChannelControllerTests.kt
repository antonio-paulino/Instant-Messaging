package im

import im.api.model.input.body.ChannelCreationInputModel
import im.api.model.input.body.ChannelRoleUpdateInputModel
import im.api.model.output.channel.ChannelCreationOutputModel
import im.api.model.output.channel.ChannelOutputModel
import im.api.model.output.channel.ChannelsOutputModel
import im.api.model.problems.ProblemOutputModel
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.user.User
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
abstract class ChannelControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser1: User = User(1L, "testUser1", "testPassword1", "test@isel.pt")
    private var testUser2: User = User(2L, "testUser2", "testPassword2", "test2@isel.pt")

    private var testSession1 = Session(0L, testUser1, LocalDateTime.now().plusMinutes(30))
    private var testSession2 = Session(0L, testUser2, LocalDateTime.now().plusMinutes(30))

    private var accessToken1 = AccessToken(UUID.randomUUID(), testSession1, LocalDateTime.now().plusMinutes(30))
    private var accessToken2 = AccessToken(UUID.randomUUID(), testSession2, LocalDateTime.now().plusMinutes(30))

    private var testChannel1 = Channel(0L, "testChannel", ChannelRole.GUEST, testUser1, true)

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
            testUser2 = userRepository.save(testUser2)
            testSession1 = sessionRepository.save(testSession1.copy(user = testUser1))
            testSession2 = sessionRepository.save(testSession2.copy(user = testUser2))
            accessToken1 = accessTokenRepository.save(accessToken1.copy(session = testSession1))
            accessToken2 = accessTokenRepository.save(accessToken2.copy(session = testSession2))
            testChannel1 = testChannel1.copy(owner = testUser1, membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER) })
        }
    }

    @Test
    fun `create channel no auth`() {
        val client = getClient()

        client
            .post()
            .uri("api/channels")
            .bodyValue(mapOf("name" to "testChannel", "isPublic" to true))
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `create channel invalid auth`() {
        val client = getClient()

        client
            .post()
            .uri("api/channels")
            .cookie("access_token", "randomstring")
            .bodyValue(mapOf("name" to "testChannel", "isPublic" to true))
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `create channel should create a channel 201`() {
        val client = getClient()

        client
            .post()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(testChannel1)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .valueMatches("Location", "/api/channels/\\d+")
            .expectBody(ChannelCreationOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertNotNull(it!!.id)
            }
    }

    @Test
    fun `create channel channel already exists 409`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.save(testChannel1)
        }

        client
            .post()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(testChannel1)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("channel-already-exists", it!!.title)
            }
    }

    @Test
    fun `get channel by id no auth`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get channel by id should return the channel 200`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .get()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(channel.id.value, it!!.id)
                assertEquals(channel.name.value, it.name)
                assertEquals(channel.isPublic, it.isPublic)
            }
    }

    @Test
    fun `get channel by id non existing channel 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/1")
            .cookie("access_token", accessToken1.token.toString())
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
    fun `get public channel by id not a member of the channel 200`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .get()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(channel.id.value, it!!.id)
                assertEquals(channel.name.value, it.name)
                assertEquals(channel.isPublic, it.isPublic)
            }
    }

    @Test
    fun `get private channel by id not a member of the channel 403`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1.copy(isPublic = false))
            }

        client
            .get()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken2.token.toString())
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
    fun `test update channel no auth`() {
        val client = getClient()

        client
            .put()
            .uri("api/channels/1")
            .bodyValue(mapOf("name" to "newName", "isPublic" to false))
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `update channel should update the channel 204`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        val update = ChannelCreationInputModel("newName", ChannelRole.MEMBER, false)

        client
            .put()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(update)
            .exchange()
            .expectStatus()
            .isNoContent

        val updatedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id)
            }

        assertNotNull(updatedChannel)
        assertEquals("newName", updatedChannel!!.name.value)
        assertEquals(false, updatedChannel.isPublic)
        assertEquals(ChannelRole.MEMBER, updatedChannel.defaultRole)
    }

    @Test
    fun `update channel non existing channel 404`() {
        val client = getClient()

        val update = ChannelCreationInputModel("newName", ChannelRole.MEMBER, false)

        client
            .put()
            .uri("api/channels/1")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(update)
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
    fun `update channel not the owner of the channel`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        val update = ChannelCreationInputModel("newName", ChannelRole.MEMBER, false)

        client
            .put()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(update)
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-update-channel", it!!.title)
            }
    }

    @Test
    fun `delete channel should delete the channel`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .delete()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNoContent

        val deletedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id)
            }

        assertEquals(null, deletedChannel)
    }

    @Test
    fun `delete channel no auth 401`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `delete channel non existing channel 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/1")
            .cookie("access_token", accessToken1.token.toString())
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
    fun `delete channel not the owner of the channel 403`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .delete()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-delete-channel", it!!.title)
            }
    }

    @Test
    fun `join channel should join the channel 204`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .put()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .valueMatches("Location", "/api/channels/${channel.id}/members/\\d+")

        val updatedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id).also { it?.members }
            }

        assertNotNull(updatedChannel)
        assertEquals(2, updatedChannel!!.members.size)
        assertEquals(updatedChannel.members[testUser2], ChannelRole.GUEST)
    }

    @Test
    fun `join channel non existing channel 404`() {
        val client = getClient()

        client
            .put()
            .uri("api/channels/1/members/1")
            .cookie("access_token", accessToken1.token.toString())
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
    fun `join channel cannot add other user 403`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .put()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-add-member", it!!.title)
            }
    }

    @Test
    fun `join channel private channel 403`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1.copy(isPublic = false))
            }

        client
            .put()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-join-private-channel", it!!.title)
            }
    }

    @Test
    fun `join channel user already in the channel 400`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .put()
            .uri("api/channels/${channel.id}/members/${testUser1.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("user-already-member", it!!.title)
            }
    }

    @Test
    fun `remove channel member no auth`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/1/members/1")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `remove channel member user should leave channel 204`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) }),
                )
            }

        client
            .delete()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isNoContent

        val updatedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id).also { it?.members }
            }

        assertNotNull(updatedChannel)
        assertEquals(1, updatedChannel!!.members.size)
        assertTrue(updatedChannel.members.containsKey(testUser1))
    }

    @Test
    fun `remove channel member owner should kick member and member cannot access private channel`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(
                            membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) },
                            isPublic = false,
                        ),
                )
            }

        client
            .delete()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNoContent

        val updatedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id).also { it?.members }
            }

        assertNotNull(updatedChannel)
        assertEquals(1, updatedChannel!!.members.size)
        assertTrue(updatedChannel.members.containsKey(testUser1))
        client
            .get()
            .uri("api/channels/${channel.id}")
            .cookie("access_token", accessToken2.token.toString())
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
    fun `remove channel member, member not in channel 404`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER) }),
                )
            }

        client
            .delete()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("user-not-found", it!!.title)
            }
    }

    @Test
    fun `remove channel member non existing channel 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/1/members/1")
            .cookie("access_token", accessToken1.token.toString())
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
    fun `remove channel member user not found 404`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER) }),
                )
            }

        client
            .delete()
            .uri("api/channels/${channel.id}/members/0")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("user-not-found", it!!.title)
            }
    }

    @Test
    fun `remove channel member cannot remove user 403`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) }),
                )
            }

        client
            .delete()
            .uri("api/channels/${channel.id}/members/${testUser1.id}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-remove-member", it!!.title)
            }
    }

    @Test
    fun `update member role no auth`() {
        val client = getClient()

        client
            .patch()
            .uri("api/channels/1/members/1")
            .bodyValue(ChannelRole.OWNER)
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `update member role should update the member role`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    Channel(0L, "testChannel", ChannelRole.MEMBER, testUser1, true)
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) }),
                )
            }

        client
            .patch()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(ChannelRoleUpdateInputModel(ChannelRole.GUEST.name))
            .exchange()
            .expectStatus()
            .isNoContent

        val updatedChannel =
            transactionManager.run {
                channelRepository.findById(channel.id).also { it?.members }
            }

        assertNotNull(updatedChannel)
        assertEquals(ChannelRole.GUEST, updatedChannel!!.members[testUser2])
    }

    @Test
    fun `update member role non existing channel`() {
        val client = getClient()

        client
            .patch()
            .uri("api/channels/1/members/1")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(ChannelRoleUpdateInputModel(ChannelRole.OWNER.name))
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
    fun `update member role user not found`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.GUEST) }),
                )
            }

        client
            .patch()
            .uri("api/channels/${channel.id}/members/0")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(ChannelRoleUpdateInputModel(ChannelRole.GUEST.name))
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("user-not-found", it!!.title)
            }
    }

    @Test
    fun `update member role, cannot update member role to owner`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) }),
                )
            }

        client
            .patch()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(ChannelRoleUpdateInputModel(ChannelRole.OWNER.name))
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-update-member-role", it!!.title)
            }
    }

    @Test
    fun `update member role, not owner of the channel`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(
                    testChannel1
                        .copy(membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) }),
                )
            }

        client
            .patch()
            .uri("api/channels/${channel.id}/members/${testUser2.id}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(ChannelRoleUpdateInputModel(ChannelRole.GUEST.name))
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-update-member-role", it!!.title)
            }
    }

    @Test
    fun `get channels should return channels 200`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .get()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.channels.size)
                assertEquals(channel.id.value, it.channels.first().id)
                assertEquals(channel.name.value, it.channels.first().name)
                assertEquals(channel.isPublic, it.channels.first().isPublic)
            }
    }

    @Test
    fun `get channels no auth`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get channels should return empty 200`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(0, it!!.channels.size)
            }
    }

    @Test
    fun `get channels should return channels with name 200`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .get()
            .uri("api/channels?name=test")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.channels.size)
                assertEquals(channel.id.value, it.channels.first().id)
                assertEquals(channel.name.value, it.channels.first().name)
                assertEquals(channel.isPublic, it.channels.first().isPublic)
            }
    }

    @Test
    fun `get channels with name should return empty`() {
        val client = getClient()

        transactionManager.run {
            channelRepository.save(testChannel1)
        }

        client
            .get()
            .uri("api/channels?name=notFound")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(0, it!!.channels.size)
            }
    }

    @Test
    fun `get channels with pagination should return channels`() {
        val client = getClient()

        val channel =
            transactionManager.run {
                channelRepository.save(testChannel1)
            }

        client
            .get()
            .uri("api/channels?page=1&size=1")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(ChannelsOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.channels.size)
                assertEquals(channel.id.value, it.channels.first().id)
                assertEquals(channel.name.value, it.channels.first().name)
                assertEquals(channel.isPublic, it.channels.first().isPublic)
            }
    }

    @Test
    fun `get channels with pagination input error 400`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels?page=-1&size=1")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals("invalid-input", it!!.title)
            }
    }

    @Test
    fun `create channels with empty name should return 400`() {
        val client = getClient()

        val channelInput = ChannelCreationInputModel("", ChannelRole.MEMBER, true)

        client
            .post()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(channelInput)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertEquals("invalid-input", it!!.title)
            }
    }

    @Test
    fun `create channel with name exceeding max length should return 400`() {
        val client = getClient()

        val longName = "a".repeat(51)
        val channelInput = ChannelCreationInputModel(longName, ChannelRole.MEMBER, true)

        client
            .post()
            .uri("api/channels")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(channelInput)
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
