package im.repositories

import im.TestApp
import im.invitations.ImInvitation
import im.invitations.ImInvitationStatus
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ImInvitationRepositoryTest(
    @Autowired private val imInvitationRepository: ImInvitationRepositoryImpl
) {

    private lateinit var testInvitation1: ImInvitation
    private lateinit var testInvitation2: ImInvitation
    private lateinit var testInvitation3: ImInvitation

    @BeforeEach
    fun setUp() {
        imInvitationRepository.deleteAll()

        testInvitation1 = ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().plusDays(1)
        )
        testInvitation2 = ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().plusHours(10)
        )
        testInvitation3 = ImInvitation(
            UUID.randomUUID(),
            ImInvitationStatus.PENDING,
            LocalDateTime.now().plusHours(5)
        )
    }

    @Test
    @Transactional
    open fun `should save an invitation`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        assertNotNull(savedInvitation.token)
        assertEquals(testInvitation1.status, savedInvitation.status)
        assertEquals(testInvitation1.expiresAt, savedInvitation.expiresAt)
    }

    @Test
    @Transactional
    open fun `should find all invitations`() {
        imInvitationRepository.save(testInvitation1)
        imInvitationRepository.save(testInvitation2)
        val invitations = imInvitationRepository.findAll()
        assertEquals(2, invitations.count())
    }

    @Test
    @Transactional
    open fun `should find invitation by id`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        val foundInvitation = imInvitationRepository.findById(savedInvitation.token)
        assertNotNull(foundInvitation)
        assertEquals(savedInvitation.token, foundInvitation!!.token)
    }

    @Test
    @Transactional
    open fun `should return null for non-existent id`() {
        val foundInvitation = imInvitationRepository.findById(UUID.randomUUID())
        assertNull(foundInvitation)
    }

    @Test
    @Transactional
    open fun `should find first page of invitations ordered by expiration`() {
        imInvitationRepository.save(testInvitation1)
        imInvitationRepository.save(testInvitation2)
        imInvitationRepository.save(testInvitation3)
        val invitations = imInvitationRepository.findFirst(0, 1)
        assertEquals(1, invitations.size)
        assertEquals(testInvitation3.token, invitations.first().token)
    }

    @Test
    @Transactional
    open fun `should update invitation`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        val updatedInvitation = savedInvitation.copy(status = ImInvitationStatus.USED)
        val result = imInvitationRepository.save(updatedInvitation)
        assertEquals(ImInvitationStatus.USED, result.status)
    }

    @Test
    open fun `use already used invitation should fail`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        val usedInvitation = savedInvitation.use()
        val result = imInvitationRepository.save(usedInvitation)
        assertEquals(ImInvitationStatus.USED, result.status)
        val usedAgainInvitation = result.use()
        val exception = assertThrows<Exception> {
            imInvitationRepository.save(usedAgainInvitation)
        }
        assertTrue(exception.message!!.contains("Cannot set status to USED if it is already USED"))
    }

    @Test
    @Transactional
    open fun `should find last page of invitations ordered by expiration desc`() {
        imInvitationRepository.save(testInvitation1)
        imInvitationRepository.save(testInvitation2)
        imInvitationRepository.save(testInvitation3)
        val lastInvitations = imInvitationRepository.findLast(0, 2)
        assertEquals(2, lastInvitations.size)
        assertEquals(testInvitation1.token, lastInvitations.first().token)
        assertEquals(testInvitation2.token, lastInvitations.last().token)
    }

    @Test
    @Transactional
    open fun `should save all invitations`() {
        val invitations = listOf(testInvitation1, testInvitation2)
        val savedInvitations = imInvitationRepository.saveAll(invitations)
        assertEquals(2, savedInvitations.size)
    }

    @Test
    @Transactional
    open fun `should find all invitations by ids`() {
        val savedInvitation1 = imInvitationRepository.save(testInvitation1)
        val savedInvitation2 = imInvitationRepository.save(testInvitation2)
        val invitations = imInvitationRepository.findAllById(
            listOf(
                savedInvitation1.token,
                savedInvitation2.token
            )
        )
        assertEquals(2, invitations.count())
    }

    @Test
    @Transactional
    open fun `should delete invitation by id`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        imInvitationRepository.deleteById(savedInvitation.token)
        assertEquals(0, imInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete multiple invitations by ids`() {
        val savedInvitation1 = imInvitationRepository.save(testInvitation1)
        val savedInvitation2 = imInvitationRepository.save(testInvitation2)
        imInvitationRepository.deleteAllById(listOf(savedInvitation1.token, savedInvitation2.token))
        assertEquals(0, imInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete invitation entity`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        imInvitationRepository.delete(savedInvitation)
        assertEquals(0, imInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all invitations`() {
        imInvitationRepository.save(testInvitation1)
        imInvitationRepository.save(testInvitation2)
        imInvitationRepository.deleteAll()
        assertEquals(0, imInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `should handle save of empty list`() {
        val result = imInvitationRepository.saveAll(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    @Transactional
    open fun `should handle delete of empty list`() {
        imInvitationRepository.deleteAll(emptyList())
        assertEquals(0, imInvitationRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true for existing invitation`() {
        val savedInvitation = imInvitationRepository.save(testInvitation1)
        assertTrue(imInvitationRepository.existsById(savedInvitation.token))
    }

    @Test
    @Transactional
    open fun `exists by id should return false for non-existing invitation`() {
        assertFalse(imInvitationRepository.existsById(UUID.randomUUID()))
    }

    @Test
    @Transactional
    open fun `count should return correct number of invitations`() {
        imInvitationRepository.save(testInvitation1)
        imInvitationRepository.save(testInvitation2)
        assertEquals(2, imInvitationRepository.count())
    }
}
