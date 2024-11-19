package im

import im.api.model.input.body.ChannelInvitationCreationInputModel
import im.api.model.input.body.ChannelInvitationUpdateInputModel
import im.api.model.input.body.InvitationAcceptInputModel
import im.api.model.output.invitations.ChannelInvitationCreationOutputModel
import im.api.model.output.invitations.ChannelInvitationOutputModel
import im.api.model.output.invitations.ChannelInvitationsPaginatedOutputModel
import im.api.model.problems.ProblemOutputModel
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.sessions.Session
import im.domain.tokens.AccessToken
import im.domain.user.User
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Profile
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Profile("!rateLimit")
abstract class InvitationsControllerTests {
    @LocalServerPort
    protected var port: Int = 0

    private fun getBaseUrl(): String = "http://localhost:$port"

    private fun getClient() = WebTestClient.bindToServer().baseUrl(getBaseUrl()).build()

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser1: User = User(1L, "testUser1", "testPassword1", "test@isel.pt")
    private var testUser2: User = User(2L, "testUser2", "testPassword2", "test2@isel.pt")
    private var testUser3 = User(3L, "testUser3", "testPassword3", "iseldaw3@isel.pt")

    private var testSession1 = Session(0L, testUser1, LocalDateTime.now().plusMinutes(30))
    private var testSession2 = Session(0L, testUser2, LocalDateTime.now().plusMinutes(30))
    private var testSession3 = Session(0L, testUser3, LocalDateTime.now().plusMinutes(30))

    private var accessToken1 = AccessToken(UUID.randomUUID(), testSession1, LocalDateTime.now().plusMinutes(30))
    private var accessToken2 = AccessToken(UUID.randomUUID(), testSession2, LocalDateTime.now().plusMinutes(30))
    private var accessToken3 = AccessToken(UUID.randomUUID(), testSession3, LocalDateTime.now().plusMinutes(30))

    private var testChannel = Channel(1L, "testChannel", ChannelRole.GUEST, testUser1, true)
    private var testChannel2 = Channel(2L, "testChannel2", ChannelRole.MEMBER, testUser2, true)

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
            testUser3 = userRepository.save(testUser3)
            testChannel = testChannel.copy(owner = testUser1, membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER) })
            testChannel = channelRepository.save(testChannel)
            testChannel2 = testChannel2.copy(owner = testUser2, membersLazy = lazy { mapOf(testUser2 to ChannelRole.OWNER) })
            testChannel2 = channelRepository.save(testChannel2)
            testSession1 = sessionRepository.save(testSession1.copy(user = testUser1))
            testSession2 = sessionRepository.save(testSession2.copy(user = testUser2))
            testSession3 = sessionRepository.save(testSession3.copy(user = testUser3))
            accessToken1 = accessTokenRepository.save(accessToken1.copy(session = testSession1))
            accessToken2 = accessTokenRepository.save(accessToken2.copy(session = testSession2))
            accessToken3 = accessTokenRepository.save(accessToken3.copy(session = testSession3))
        }
    }

    @Test
    fun `create invitation no authentication`() {
        val client = getClient()

        client
            .post()
            .uri("/api/channels/0/invitations")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `create invitation successfully 201`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isCreated
            .expectHeader()
            .valueMatches("Location", "/api/channels/${testChannel.id.value}/invitations/\\d+")
            .expectBody(ChannelInvitationCreationOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertNotNull(it!!.id)
            }
    }

    @Test
    fun `fail to create invitation for non-existing channel 404`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/999/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
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
    fun `fail to create invitation for invitee already a member`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser1.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitee-already-member", it!!.title)
            }
    }

    @Test
    fun `fail to create invitation with invalid expiration date`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusMinutes(5),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-invitation", it!!.title)
            }
    }

    @Test
    fun `fail to create invitation with non-existing invitee 404`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                "999",
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
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
    fun `fail to create invitation invite already exists 400`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isCreated

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitation-already-exists", it!!.title)
            }
    }

    @Test
    fun `fail to create invitation no permission 403`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(invitation)
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("user-cannot-invite-to-channel", it!!.title)
            }
    }

    @Test
    fun `get invitation no auth 401`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations/0")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get invitation should return invitation 200`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectBody(ChannelInvitationOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(invitation.id.value, it!!.id)
                assertEquals(invitation.invitee.id.value, it.invitee.id)
                assertEquals(invitation.status.name, it.status)
                assertEquals(
                    invitation.expiresAt.truncatedTo(ChronoUnit.MILLIS),
                    it.expiresAt.truncatedTo(ChronoUnit.MILLIS),
                )
            }
    }

    @Test
    fun `get invitation, invitation not found 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations/999")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitation-not-found", it!!.title)
            }
    }

    @Test
    fun `get invitation, channel not found 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/999/invitations/999")
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
    fun `get invitation user cannot access 403`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken3.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-invitation", it!!.title)
            }
    }

    @Test
    fun `get channel invitations no auth 401`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get channel invitations should return invitations 200`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectBody(ChannelInvitationsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.invitations.size)
                assertEquals(invitation.id.value, it.invitations[0].id)
            }
    }

    @Test
    fun `get channel invitations, channel not found 404`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/999/invitations")
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
    fun `get channel invitations, user cannot access 403`() {
        val client = getClient()

        client
            .get()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-invitation", it!!.title)
            }
    }

    @Test
    fun `update invitation no auth 401`() {
        val client = getClient()

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/0")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `update invitation should update invitation 204`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusDays(5),
            )

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(updatedInvitation)
            .exchange()
            .expectStatus()
            .isNoContent

        val updated =
            transactionManager.run {
                channelInvitationRepository.findById(invitation.id)
            }

        assertNotNull(updated)
        assertEquals(updatedInvitation.role.value, updated!!.role.toString())
        assertEquals(
            updatedInvitation.expiresAt.truncatedTo(ChronoUnit.MILLIS),
            updated.expiresAt.truncatedTo(ChronoUnit.MILLIS),
        )
    }

    @Test
    fun `update invitation, invitation not found 404`() {
        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusDays(5),
            )

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/999")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(updatedInvitation)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitation-not-found", it!!.title)
            }
    }

    @Test
    fun `update invitation, channel not found 404`() {
        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusDays(5),
            )

        client
            .patch()
            .uri("api/channels/999/invitations/999")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(updatedInvitation)
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
    fun `update invitation, user cannot update 403`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusDays(5),
            )

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(updatedInvitation)
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-update-invitation", it!!.title)
            }
    }

    @Test
    fun `update expired invitation should fail 400`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().minusDays(1),
                    ),
                )
            }

        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusDays(5),
            )

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(updatedInvitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-invitation", it!!.title)
            }
    }

    @Test
    fun `update invitation invalid expiration date 400`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val updatedInvitation =
            ChannelInvitationUpdateInputModel(
                "GUEST",
                LocalDateTime.now().plusMonths(2),
            )

        client
            .patch()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(updatedInvitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-invitation", it!!.title)
            }
    }

    @Test
    fun `delete invitation no auth 401`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id.value}/invitations/0")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `delete invitation successfully 204`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNoContent
    }

    @Test
    fun `delete invitation channel not found 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/999/invitations/999")
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
    fun `delete invitation invitation not found 404`() {
        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id.value}/invitations/999")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitation-not-found", it!!.title)
            }
    }

    @Test
    fun `fail to delete invitation as non-inviter 403`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .delete()
            .uri("api/channels/${testChannel.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-delete-invitation", it!!.title)
            }
    }

    @Test
    fun `get user invitations should return invitations 200`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        client
            .get()
            .uri("api/users/${testUser2.id.value}/invitations")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectBodyList(ChannelInvitationsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(1, it!!.size)
                assertEquals(invitation.id.value, it[0].invitations[0].id)
            }
    }

    @Test
    fun `get user invitations should return empty list 200`() {
        val client = getClient()

        client
            .get()
            .uri("api/users/${testUser2.id.value}/invitations")
            .cookie("access_token", accessToken2.token.toString())
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType("application/json")
            .expectBody(ChannelInvitationsPaginatedOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals(0, it!!.invitations.size)
            }
    }

    @Test
    fun `get user invitations, user cannot access`() {
        val client = getClient()

        client
            .get()
            .uri("api/users/${testUser2.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-invitation", it!!.title)
            }
    }

    @Test
    fun `accept expired invitation 400`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().minusDays(1),
                    ),
                )
            }

        val client = getClient()

        val newInvitation = InvitationAcceptInputModel("ACCEPTED")

        client
            .patch()
            .uri("api/users/${testUser2.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(newInvitation)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invalid-invitation", it!!.title)
            }
    }

    @Test
    fun `accept invitation, should accept invitation 204`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val newInvitation = InvitationAcceptInputModel("ACCEPTED")

        client
            .patch()
            .uri("api/users/${testUser2.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(newInvitation)
            .exchange()
            .expectStatus()
            .isNoContent

        val updated =
            transactionManager.run {
                channelInvitationRepository.findById(invitation.id).also { it?.channel?.members }
            }
        val channel =
            transactionManager.run {
                channelRepository.findById(testChannel.id).also { it?.members }
            }

        assertNotNull(updated)
        assertNotNull(channel)
        assertEquals(invitation.id, updated!!.id)
        assertEquals(ChannelInvitationStatus.ACCEPTED, updated.status)
        assertEquals(invitation.channel.id, updated.channel.id)
        assertEquals(1, channel!!.members.keys.count { it.id == testUser2.id })
    }

    @Test
    fun `reject invitation, should reject invitation 204`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val newInvitation = InvitationAcceptInputModel("REJECTED")

        client
            .patch()
            .uri("api/users/${testUser2.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(newInvitation)
            .exchange()
            .expectStatus()
            .isNoContent

        val updated =
            transactionManager.run {
                channelInvitationRepository.findById(invitation.id)
            }

        assertNotNull(updated)
        assertEquals(invitation.id, updated!!.id)
        assertEquals(ChannelInvitationStatus.REJECTED, updated.status)
        assertEquals(invitation.channel.id, updated.channel.id)
        assertFalse(invitation.invitee in updated.channel.members.keys)
    }

    @Test
    fun `accept invitation, invitation not found 404`() {
        val client = getClient()

        val newInvitation = InvitationAcceptInputModel("ACCEPTED")

        client
            .patch()
            .uri("api/users/${testUser2.id.value}/invitations/999")
            .cookie("access_token", accessToken2.token.toString())
            .bodyValue(newInvitation)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("invitation-not-found", it!!.title)
            }
    }

    @Test
    fun `accept invitation, user cannot access`() {
        val invitation =
            transactionManager.run {
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = testChannel,
                        invitee = testUser2,
                        inviter = testUser1,
                        role = ChannelRole.MEMBER,
                        status = ChannelInvitationStatus.PENDING,
                        expiresAt = LocalDateTime.now().plusDays(1),
                    ),
                )
            }

        val client = getClient()

        val newInvitation = InvitationAcceptInputModel("ACCEPTED")

        client
            .patch()
            .uri("api/users/${testUser2.id.value}/invitations/${invitation.id.value}")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(newInvitation)
            .exchange()
            .expectStatus()
            .isForbidden
            .expectBody(ProblemOutputModel::class.java)
            .returnResult()
            .responseBody
            .also {
                assertNotNull(it)
                assertEquals("cannot-access-invitation", it!!.title)
            }
    }

    @Test
    fun `create invitation with invalid role should return 400`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().plusDays(1),
                "INVALID_ROLE",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
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
    fun `create invitation with invalid expiration date format should return 400`() {
        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(
                mapOf(
                    "inviteeId" to testUser2.id.value.toString(),
                    "expiresAt" to "invalid-date-format",
                    "role" to "MEMBER",
                ),
            ).exchange()
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
    fun `create invitation with invalid invitee ID format should return 400`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                "invalid-id",
                LocalDateTime.now().plusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
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
    fun `create invitation with expiration date in the past should return 400`() {
        val invitation =
            ChannelInvitationCreationInputModel(
                testUser2.id.value.toString(),
                LocalDateTime.now().minusDays(1),
                "MEMBER",
            )

        val client = getClient()

        client
            .post()
            .uri("api/channels/${testChannel.id.value}/invitations")
            .cookie("access_token", accessToken1.token.toString())
            .bodyValue(invitation)
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
