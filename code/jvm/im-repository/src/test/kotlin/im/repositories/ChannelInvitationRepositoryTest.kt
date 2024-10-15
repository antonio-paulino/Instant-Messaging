package im.repositories

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import im.domain.wrappers.toIdentifier
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class ChannelInvitationRepositoryTest {
    private lateinit var testChannel: Channel
    private lateinit var testInviter: User
    private lateinit var testInvitee: User
    private lateinit var testInvitee2: User
    private lateinit var testInvitee3: User
    private lateinit var testInvitation1: ChannelInvitation
    private lateinit var testInvitation2: ChannelInvitation
    private lateinit var testInvitation3: ChannelInvitation

    @Autowired
    private lateinit var transactionManager: TransactionManager

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
        insertData()
    }

    private fun insertData() {
        transactionManager.run {
            testInviter = userRepository.save(User(1, "Inviter", "password", "user1@daw.isel.pt"))
            testInvitee = userRepository.save(User(2, "Invitee", "password", "user2@daw.isel.pt"))
            testInvitee2 = userRepository.save(User(3, "Invitee2", "password", "user3@dawisel.pt"))
            testInvitee3 = userRepository.save(User(4, "Invitee3", "password", "user4@dawisel.pt"))
            testChannel = channelRepository.save(Channel(1, "General", testInviter, true))

            testInvitation1 =
                ChannelInvitation(
                    1L,
                    testChannel,
                    testInviter,
                    testInvitee,
                    ChannelInvitationStatus.PENDING,
                    ChannelRole.MEMBER,
                    LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                )
            testInvitation2 =
                ChannelInvitation(
                    2L,
                    testChannel,
                    testInviter,
                    testInvitee2,
                    ChannelInvitationStatus.PENDING,
                    ChannelRole.MEMBER,
                    LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS),
                )
            testInvitation3 =
                ChannelInvitation(
                    3L,
                    testChannel,
                    testInviter,
                    testInvitee3,
                    ChannelInvitationStatus.PENDING,
                    ChannelRole.GUEST,
                    LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.MILLIS),
                )
        }
    }

    @Test
    open fun `should save a channel invitation`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            assertNotNull(savedInvitation.id)
            assertEquals(testInvitation1.status, savedInvitation.status)
            assertEquals(testInvitation1.role, savedInvitation.role)
            assertEquals(testInvitation1.expiresAt, savedInvitation.expiresAt)
            assertEquals(testInvitation1.inviter, savedInvitation.inviter)
            assertEquals(testInvitation1.invitee, savedInvitation.invitee)
        }
    }

    @Test
    open fun `should save multiple channel invitations`() {
        transactionManager.run {
            val invitations = listOf(testInvitation1, testInvitation2)
            val savedInvitations = channelInvitationRepository.saveAll(invitations)
            assertEquals(2, savedInvitations.size)
        }
    }

    @Test
    open fun `should delete on channel delete`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            assertEquals(1, channelInvitationRepository.count())
            channelRepository.delete(testChannel)

            channelRepository.flush()
            channelInvitationRepository.flush()

            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete on inviter delete`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            assertEquals(1, channelInvitationRepository.count())
            userRepository.delete(testInviter)

            userRepository.flush()
            channelInvitationRepository.flush()

            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete on invitee delete`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            assertEquals(1, channelInvitationRepository.count())
            userRepository.delete(testInvitee)

            userRepository.flush()
            channelInvitationRepository.flush()

            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete expired invitations`() {
        transactionManager.run {
            testInvitation1 = testInvitation1.copy(expiresAt = LocalDateTime.now().minusDays(1))
            channelInvitationRepository.save(testInvitation1)
            assertEquals(1, channelInvitationRepository.count())
            channelInvitationRepository.deleteExpired()
            assertEquals(0, channelInvitationRepository.count())
            channelInvitationRepository.save(testInvitation1)
        }
    }

    @Test
    open fun `should find channel invitation by id`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            val foundInvitation = channelInvitationRepository.findById(savedInvitation.id)
            assertNotNull(foundInvitation)
            assertEquals(savedInvitation, foundInvitation)
        }
    }

    @Test
    open fun `should return null for non-existent id`() {
        transactionManager.run {
            val foundInvitation = channelInvitationRepository.findById((999L).toIdentifier())
            assertNull(foundInvitation)
        }
    }

    @Test
    open fun `should find all channel invitations`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)
            val invitations = channelInvitationRepository.findAll()
            assertEquals(2, invitations.count())
        }
    }

    @Test
    open fun `should find first page of invitations`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)

            val (invitations, pagination) =
                channelInvitationRepository.find(
                    PaginationRequest(
                        1,
                        1,
                    ),
                    SortRequest("id"),
                )

            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, invitations.size)
            assertEquals(testInvitation1.inviter, invitations.first().inviter)
            assertEquals(testInvitation1.invitee, invitations.first().invitee)
            assertEquals(testInvitation1.expiresAt, invitations.first().expiresAt)
            assertEquals(testInvitation1.role, invitations.first().role)
        }
    }

    @Test
    open fun `get invitations by channel should be empty`() {
        transactionManager.run {
            val invitations =
                channelInvitationRepository
                    .findByChannel(
                        testChannel,
                        ChannelInvitationStatus.PENDING,
                        SortRequest("id"),
                        PaginationRequest(1, 1),
                    ).items
            assertTrue(invitations.isEmpty())
        }
    }

    @Test
    open fun `get invitations by channel should return one invitation`() {
        transactionManager.run {
            testInvitation1 = testInvitation1.copy(channel = testChannel)
            channelInvitationRepository.save(testInvitation1)
            val invitations =
                channelInvitationRepository.findByChannel(
                    testChannel,
                    ChannelInvitationStatus.PENDING,
                    SortRequest("id"),
                    PaginationRequest(1, 1),
                )
            assertEquals(1, invitations.items.size)
        }
    }

    @Test
    open fun `find by invitee and channel should return null`() {
        transactionManager.run {
            val invitation = channelInvitationRepository.findByInviteeAndChannel(testInvitee, testChannel)
            assertNull(invitation)
        }
    }

    @Test
    open fun `find by invitee and channel should return invitation`() {
        transactionManager.run {
            testInvitation1 = testInvitation1.copy(channel = testChannel)
            testInvitation1 = channelInvitationRepository.save(testInvitation1)
            val invitation = channelInvitationRepository.findByInviteeAndChannel(testInvitee, testChannel)
            assertNotNull(invitation)
            assertEquals(testInvitation1, invitation)
        }
    }

    @Test
    open fun `get invitations by user should return empty list`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)

            val channels =
                channelInvitationRepository.findByInvitee(
                    testInviter,
                    ChannelInvitationStatus.PENDING,
                    SortRequest("id"),
                    PaginationRequest(1, 1),
                )
            assertTrue(channels.items.none())
        }
    }

    @Test
    open fun `get invitations by user should return 1 invitation`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)

            val channels =
                channelInvitationRepository
                    .findByInvitee(
                        testInvitee,
                        ChannelInvitationStatus.PENDING,
                        SortRequest("id"),
                        PaginationRequest(1, 1),
                    ).items
            assertEquals(1, channels.size)
            assertEquals(testChannel, channels.first().channel)
            assertEquals(testInviter, channels.first().inviter)
        }
    }

    @Test
    open fun `should update channel invitation`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            val updatedInvitation = savedInvitation.copy(status = ChannelInvitationStatus.ACCEPTED)
            val result = channelInvitationRepository.save(updatedInvitation)
            assertEquals(ChannelInvitationStatus.ACCEPTED, result.status)
        }
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)
            val (invitations, pagination) = channelInvitationRepository.find(PaginationRequest(1, 1, getCount = false), SortRequest("id"))
            assertEquals(1, invitations.size)
            assertNotNull(pagination)
            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertNull(pagination.total)
            assertNull(pagination.totalPages)
        }
    }

    @Test
    open fun `should find last page of invitations ordered by id`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)
            channelInvitationRepository.save(testInvitation3)

            val (lastInvitations, pagination) =
                channelInvitationRepository.find(
                    PaginationRequest(
                        1,
                        2,
                    ),
                    SortRequest("id", Sort.DESC),
                )

            // pagination
            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(3, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(2, lastInvitations.size)
            assertEquals(testInvitation3.role, lastInvitations.first().role)
            assertEquals(testInvitation3.expiresAt, lastInvitations.first().expiresAt)
            assertEquals(testInvitation3.invitee, lastInvitations.first().invitee)
            assertEquals(testInvitation2.role, lastInvitations.last().role)
            assertEquals(testInvitation2.expiresAt, lastInvitations.last().expiresAt)
            assertEquals(testInvitation2.invitee, lastInvitations.last().invitee)
        }
    }

    @Test
    open fun `should delete channel invitation by id`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.deleteById(savedInvitation.id)
            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete multiple channel invitations by ids`() {
        transactionManager.run {
            val savedInvitation1 = channelInvitationRepository.save(testInvitation1)
            val savedInvitation2 = channelInvitationRepository.save(testInvitation2)
            channelInvitationRepository.deleteAllById(listOf(savedInvitation1.id, savedInvitation2.id))
            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete channel invitation entity`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.delete(savedInvitation)
            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete all channel invitations`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)
            channelInvitationRepository.deleteAll()
            assertEquals(0, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `exists by id should return true for existing invitation`() {
        transactionManager.run {
            val savedInvitation = channelInvitationRepository.save(testInvitation1)
            assertTrue(channelInvitationRepository.existsById(savedInvitation.id))
        }
    }

    @Test
    open fun `exists by id should return false for non-existing invitation`() {
        transactionManager.run {
            assertFalse(channelInvitationRepository.existsById((999L).toIdentifier()))
        }
    }

    @Test
    open fun `count should return correct number of channel invitations`() {
        transactionManager.run {
            channelInvitationRepository.save(testInvitation1)
            channelInvitationRepository.save(testInvitation2)
            assertEquals(2, channelInvitationRepository.count())
        }
    }

    @Test
    open fun `should handle save of empty list`() {
        transactionManager.run {
            val result = channelInvitationRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Test
    open fun `should handle delete of empty list`() {
        transactionManager.run {
            channelInvitationRepository.deleteAll(emptyList())
            assertEquals(0, channelInvitationRepository.count())
        }
    }
}
