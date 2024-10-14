package im.repositories

import im.domain.invitations.ImInvitation
import im.domain.invitations.ImInvitationStatus
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
import java.util.UUID
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class ImInvitationRepositoryTest {
    @Autowired
    private lateinit var transactionManager: TransactionManager

    private lateinit var testInvitation1: ImInvitation
    private lateinit var testInvitation2: ImInvitation
    private lateinit var testInvitation3: ImInvitation

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
        testInvitation1 =
            ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
            )
        testInvitation2 =
            ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.MILLIS),
            )
        testInvitation3 =
            ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS),
            )
    }

    @Test
    open fun `should save an invitation`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            assertNotNull(savedInvitation.token)
            assertEquals(testInvitation1.status, savedInvitation.status)
            assertEquals(testInvitation1.expiresAt, savedInvitation.expiresAt)
        }
    }

    @Test
    open fun `should find all invitations`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            val invitations = imInvitationRepository.findAll()
            assertEquals(2, invitations.count())
        }
    }

    @Test
    open fun `should find invitation by id`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            val foundInvitation = imInvitationRepository.findById(savedInvitation.token)
            assertNotNull(foundInvitation)
            assertEquals(savedInvitation.token, foundInvitation!!.token)
        }
    }

    @Test
    open fun `should return null for non-existent id`() {
        transactionManager.run {
            val foundInvitation = imInvitationRepository.findById(UUID.randomUUID())
            assertNull(foundInvitation)
        }
    }

    @Test
    open fun `should find first page of invitations ordered by expiration`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.save(testInvitation3)
            val (invitations, pagination) =
                imInvitationRepository.find(
                    PaginationRequest(
                        1,
                        1,
                    ),
                    SortRequest(
                        "expiresAt",
                    ),
                )

            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(3, pagination.total)
            assertEquals(3, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, invitations.size)
            assertEquals(testInvitation3.token, invitations.first().token)
        }
    }

    @Test
    open fun `should find last page of invitations ordered by expiration desc`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.save(testInvitation3)
            val (invitations, pagination) =
                imInvitationRepository.find(
                    PaginationRequest(
                        1,
                        2,
                    ),
                    SortRequest(
                        "expiresAt",
                        Sort.DESC,
                    ),
                )

            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(3, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(2, invitations.size)
            assertEquals(testInvitation1.token, invitations.first().token)
            assertEquals(testInvitation2.token, invitations.last().token)
        }
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.save(testInvitation3)
            val (invitations, pagination) =
                imInvitationRepository.find(
                    PaginationRequest(
                        1,
                        1,
                        getCount = false,
                    ),
                    SortRequest(
                        "expiresAt",
                    ),
                )

            assertNotNull(pagination)
            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertNull(pagination.prevPage)
            assertNull(pagination.total)
            assertNull(pagination.totalPages)
            assertEquals(1, invitations.size)
            assertEquals(testInvitation3.token, invitations.first().token)
        }
    }

    @Test
    open fun `should update invitation`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            val updatedInvitation = savedInvitation.copy(status = ImInvitationStatus.USED)
            val result = imInvitationRepository.save(updatedInvitation)
            assertEquals(ImInvitationStatus.USED, result.status)
        }
    }

    @Test
    open fun `should save all invitations`() {
        transactionManager.run {
            val invitations = listOf(testInvitation1, testInvitation2)
            val savedInvitations = imInvitationRepository.saveAll(invitations)
            assertEquals(2, savedInvitations.size)
        }
    }

    @Test
    open fun `should find all invitations by ids`() {
        transactionManager.run {
            val savedInvitation1 = imInvitationRepository.save(testInvitation1)
            val savedInvitation2 = imInvitationRepository.save(testInvitation2)
            val invitations =
                imInvitationRepository.findAllById(
                    listOf(
                        savedInvitation1.token,
                        savedInvitation2.token,
                    ),
                )
            assertEquals(2, invitations.count())
        }
    }

    @Test
    open fun `should delete invitation by id`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            imInvitationRepository.deleteById(savedInvitation.token)
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete multiple invitations by ids`() {
        transactionManager.run {
            val savedInvitation1 = imInvitationRepository.save(testInvitation1)
            val savedInvitation2 = imInvitationRepository.save(testInvitation2)
            imInvitationRepository.deleteAllById(listOf(savedInvitation1.token, savedInvitation2.token))
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete invitation entity`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            imInvitationRepository.delete(savedInvitation)
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete all invitations`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.deleteAll()
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `should delete all expired invitations`() {
        transactionManager.run {
            testInvitation1 = testInvitation1.copy(expiresAt = LocalDateTime.now().minusDays(1))
            imInvitationRepository.save(testInvitation1)
            assertEquals(1, imInvitationRepository.count())
            imInvitationRepository.deleteExpired()
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `should handle save of empty list`() {
        transactionManager.run {
            val result = imInvitationRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Test
    open fun `should handle delete of empty list`() {
        transactionManager.run {
            imInvitationRepository.deleteAll(emptyList())
            assertEquals(0, imInvitationRepository.count())
        }
    }

    @Test
    open fun `exists by id should return true for existing invitation`() {
        transactionManager.run {
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            assertTrue(imInvitationRepository.existsById(savedInvitation.token))
        }
    }

    @Test
    open fun `exists by id should return false for non-existing invitation`() {
        transactionManager.run {
            assertFalse(imInvitationRepository.existsById(UUID.randomUUID()))
        }
    }

    @Test
    open fun `count should return correct number of invitations`() {
        transactionManager.run {
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            assertEquals(2, imInvitationRepository.count())
        }
    }
}
