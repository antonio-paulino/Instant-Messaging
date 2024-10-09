package im.repositories

import im.TestApp
import im.channel.Channel
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.channel.ChannelRole
import im.pagination.PaginationRequest
import im.pagination.Sort
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import im.user.User
import im.wrappers.toIdentifier
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ChannelInvitationRepositoryTest(
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val channelRepository: ChannelRepositoryImpl,
    @Autowired private val channelInvitationRepository: ChannelInvitationRepositoryImpl,
) {

    private lateinit var testChannel: Channel
    private lateinit var testInviter: User
    private lateinit var testInvitee: User
    private lateinit var testInvitation1: ChannelInvitation
    private lateinit var testInvitation2: ChannelInvitation
    private lateinit var testInvitation3: ChannelInvitation

    @BeforeEach
    fun setUp() {
        channelInvitationRepository.deleteAll()

        testInviter = userRepository.save(User(1, "Inviter", "password", "user1@daw.isel.pt"))
        testInvitee = userRepository.save(User(2, "Invitee", "password", "user2@daw.isel.pt"))
        testChannel = channelRepository.save(Channel(1, "General", testInviter, true))

        testInvitation1 = ChannelInvitation(
            1L,
            testChannel,
            testInviter,
            testInvitee,
            ChannelInvitationStatus.PENDING,
            ChannelRole.MEMBER,
            LocalDateTime.now().plusDays(1)
        )
        testInvitation2 = ChannelInvitation(
            2L,
            testChannel,
            testInviter,
            testInvitee,
            ChannelInvitationStatus.PENDING,
            ChannelRole.MEMBER,
            LocalDateTime.now().plusHours(5)
        )
        testInvitation3 = ChannelInvitation(
            3L,
            testChannel,
            testInviter,
            testInvitee,
            ChannelInvitationStatus.PENDING,
            ChannelRole.GUEST,
            LocalDateTime.now().plusHours(10)
        )
    }

    @Test
    @Transactional
    open fun `should save a channel invitation`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        assertNotNull(savedInvitation.id)
        assertEquals(testInvitation1.status, savedInvitation.status)
        assertEquals(testInvitation1.role, savedInvitation.role)
        assertEquals(testInvitation1.expiresAt, savedInvitation.expiresAt)
        assertEquals(testInvitation1.inviter, savedInvitation.inviter)
        assertEquals(testInvitation1.invitee, savedInvitation.invitee)
    }

    @Test
    @Transactional
    open fun `should save multiple channel invitations`() {
        val invitations = listOf(testInvitation1, testInvitation2)
        val savedInvitations = channelInvitationRepository.saveAll(invitations)
        assertEquals(2, savedInvitations.size)
    }

    @Test
    @Transactional
    open fun `should delete on channel delete`() {
        channelInvitationRepository.save(testInvitation1)
        assertEquals(1, channelInvitationRepository.count())
        channelRepository.delete(testChannel)

        channelRepository.flush()
        channelInvitationRepository.flush()

        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete on inviter delete`() {
        channelInvitationRepository.save(testInvitation1)
        assertEquals(1, channelInvitationRepository.count())
        userRepository.delete(testInviter)

        userRepository.flush()
        channelInvitationRepository.flush()

        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete on invitee delete`() {
        channelInvitationRepository.save(testInvitation1)
        assertEquals(1, channelInvitationRepository.count())
        userRepository.delete(testInvitee)

        userRepository.flush()
        channelInvitationRepository.flush()

        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should find channel invitation by id`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        val foundInvitation = channelInvitationRepository.findById(savedInvitation.id)
        assertNotNull(foundInvitation)
        assertEquals(savedInvitation, foundInvitation)
    }

    @Test
    @Transactional
    open fun `should return null for non-existent id`() {
        val foundInvitation = channelInvitationRepository.findById((999L).toIdentifier())
        assertNull(foundInvitation)
    }

    @Test
    @Transactional
    open fun `should find all channel invitations`() {
        channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.save(testInvitation2)
        val invitations = channelInvitationRepository.findAll()
        assertEquals(2, invitations.count())
    }

    @Test
    @Transactional
    open fun `should find first page of invitations`() {
        channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.save(testInvitation2)

        val (invitations, pagination) = channelInvitationRepository.find(PaginationRequest(1, 1))

        assertEquals(1, pagination.currentPage)
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

    @Test
    @Transactional
    open fun `should update channel invitation`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        val updatedInvitation = savedInvitation.copy(status = ChannelInvitationStatus.ACCEPTED)
        val result = channelInvitationRepository.save(updatedInvitation)
        assertEquals(ChannelInvitationStatus.ACCEPTED, result.status)
    }

    @Transactional
    @Test
    open fun `should find last page of invitations ordered by id`() {
        channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.save(testInvitation2)
        channelInvitationRepository.save(testInvitation3)

        val (lastInvitations, pagination) = channelInvitationRepository.find(PaginationRequest(1, 2, Sort.DESC))

        // pagination
        assertEquals(1, pagination.currentPage)
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

    @Test
    @Transactional
    open fun `should delete channel invitation by id`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.deleteById(savedInvitation.id)
        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete multiple channel invitations by ids`() {
        val savedInvitation1 = channelInvitationRepository.save(testInvitation1)
        val savedInvitation2 = channelInvitationRepository.save(testInvitation2)
        channelInvitationRepository.deleteAllById(listOf(savedInvitation1.id, savedInvitation2.id))
        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete channel invitation entity`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.delete(savedInvitation)
        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all channel invitations`() {
        channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.save(testInvitation2)
        channelInvitationRepository.deleteAll()
        assertEquals(0, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true for existing invitation`() {
        val savedInvitation = channelInvitationRepository.save(testInvitation1)
        assertTrue(channelInvitationRepository.existsById(savedInvitation.id))
    }

    @Test
    @Transactional
    open fun `exists by id should return false for non-existing invitation`() {
        assertFalse(channelInvitationRepository.existsById((999L).toIdentifier()))
    }

    @Test
    @Transactional
    open fun `count should return correct number of channel invitations`() {
        channelInvitationRepository.save(testInvitation1)
        channelInvitationRepository.save(testInvitation2)
        assertEquals(2, channelInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should handle save of empty list`() {
        val result = channelInvitationRepository.saveAll(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    @Transactional
    open fun `should handle delete of empty list`() {
        channelInvitationRepository.deleteAll(emptyList())
        assertEquals(0, channelInvitationRepository.count())
    }
}
