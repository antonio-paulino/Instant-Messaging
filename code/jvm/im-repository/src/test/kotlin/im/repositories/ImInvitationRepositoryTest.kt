package im.repositories

import im.TestApp
import im.invitations.ImInvitation
import im.invitations.ImInvitationStatus
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import im.repository.mem.transactions.MemTransactionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ImInvitationRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa
) {
    private lateinit var testInvitation1: ImInvitation
    private lateinit var testInvitation2: ImInvitation
    private lateinit var testInvitation3: ImInvitation

    private fun transactionManagers(): Stream<TransactionManager> =
        Stream.of(
            transactionManagerJpa.also { cleanup(it) },
            MemTransactionManager().also { cleanup(it) },
        )

    private fun cleanup(transactionManager: TransactionManager) {
        transactionManager.run({
            refreshTokenRepository.deleteAll()
            accessTokenRepository.deleteAll()
            imInvitationRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            messageRepository.deleteAll()
            channelInvitationRepository.deleteAll()
            channelRepository.deleteAll()
            sessionRepository.deleteAll()
            userRepository.deleteAll()
        })
    }

    private fun setup(transactionManager: TransactionManager) {
        transactionManager.run({
            testInvitation1 = ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )
            testInvitation2 = ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusHours(10).truncatedTo(ChronoUnit.MILLIS)
            )
            testInvitation3 = ImInvitation(
                UUID.randomUUID(),
                ImInvitationStatus.PENDING,
                LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS)
            )
        })
    }


    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save an invitation`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            assertNotNull(savedInvitation.token)
            assertEquals(testInvitation1.status, savedInvitation.status)
            assertEquals(testInvitation1.expiresAt, savedInvitation.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all invitations`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            val invitations = imInvitationRepository.findAll()
            assertEquals(2, invitations.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find invitation by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            val foundInvitation = imInvitationRepository.findById(savedInvitation.token)
            assertNotNull(foundInvitation)
            assertEquals(savedInvitation.token, foundInvitation!!.token)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null for non-existent id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundInvitation = imInvitationRepository.findById(UUID.randomUUID())
            assertNull(foundInvitation)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find first page of invitations ordered by expiration`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.save(testInvitation3)
            val (invitations, pagination) = imInvitationRepository.find(
                PaginationRequest(
                    1,
                    1
                )
            )

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(3, pagination.total)
            assertEquals(3, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, invitations.size)
            assertEquals(testInvitation3.token, invitations.first().token)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find last page of invitations ordered by expiration desc`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.save(testInvitation3)
            val (invitations, pagination) = imInvitationRepository.find(
                PaginationRequest(
                    1,
                    2,
                    Sort.DESC
                )
            )

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(3, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(2, invitations.size)
            assertEquals(testInvitation1.token, invitations.first().token)
            assertEquals(testInvitation2.token, invitations.last().token)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should update invitation`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            val updatedInvitation = savedInvitation.copy(status = ImInvitationStatus.USED)
            val result = imInvitationRepository.save(updatedInvitation)
            assertEquals(ImInvitationStatus.USED, result.status)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save all invitations`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val invitations = listOf(testInvitation1, testInvitation2)
            val savedInvitations = imInvitationRepository.saveAll(invitations)
            assertEquals(2, savedInvitations.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all invitations by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation1 = imInvitationRepository.save(testInvitation1)
            val savedInvitation2 = imInvitationRepository.save(testInvitation2)
            val invitations = imInvitationRepository.findAllById(
                listOf(
                    savedInvitation1.token,
                    savedInvitation2.token
                )
            )
            assertEquals(2, invitations.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete invitation by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            imInvitationRepository.deleteById(savedInvitation.token)
            assertEquals(0, imInvitationRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete multiple invitations by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation1 = imInvitationRepository.save(testInvitation1)
            val savedInvitation2 = imInvitationRepository.save(testInvitation2)
            imInvitationRepository.deleteAllById(listOf(savedInvitation1.token, savedInvitation2.token))
            assertEquals(0, imInvitationRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete invitation entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            imInvitationRepository.delete(savedInvitation)
            assertEquals(0, imInvitationRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all invitations`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            imInvitationRepository.deleteAll()
            assertEquals(0, imInvitationRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle save of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val result = imInvitationRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle delete of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.deleteAll(emptyList())
            assertEquals(0, imInvitationRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true for existing invitation`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedInvitation = imInvitationRepository.save(testInvitation1)
            assertTrue(imInvitationRepository.existsById(savedInvitation.token))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false for non-existing invitation`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertFalse(imInvitationRepository.existsById(UUID.randomUUID()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return correct number of invitations`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            imInvitationRepository.save(testInvitation1)
            imInvitationRepository.save(testInvitation2)
            assertEquals(2, imInvitationRepository.count())
        })
    }
}
