package im.repositories

import im.TestApp
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import im.sessions.Session
import im.repository.mem.transactions.MemTransactionManager
import im.user.User
import im.wrappers.toIdentifier
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import kotlin.test.assertEquals

private const val SESSION_DURATION_DAYS = 90L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class SessionRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa
) {

    private var testUser = User(1, "user", "password", "user1@daw.isel.pt")

    private var testSession: Session = Session(
        user = testUser,
        expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS).truncatedTo(ChronoUnit.MILLIS),
    )

    private var testSession2 = Session(
        user = testUser,
        expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1).truncatedTo(ChronoUnit.MILLIS),
    )

    private fun transactionManagers(): Stream<TransactionManager> =
        Stream.of(
            MemTransactionManager().also { cleanup(it) },
            transactionManagerJpa.also { cleanup(it) }
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
            testUser = userRepository.save(testUser)
            testSession = testSession.copy(user = testUser)
            testSession2 = testSession2.copy(user = testUser)
        })
    }


    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save session`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            assertNotNull(session.id)
            assertEquals(testSession.user, session.user)
            assertEquals(testSession.expiresAt, session.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find session by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(
                Session(
                    user = testUser,
                    expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS),
                )
            )
            val foundSession = sessionRepository.findById(session.id)
            assertNotNull(foundSession)
            assertEquals(session.user, foundSession!!.user)
            assertEquals(session.expiresAt, foundSession.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should not find by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundSession = sessionRepository.findById((9999L).toIdentifier())
            assertNull(foundSession)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all sessions`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            val sessions = sessionRepository.findAll()
            assertEquals(2, sessions.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val sessions = sessionRepository.findAll()
            assertEquals(0, sessions.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find first, first page size 1 should return 1 session`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            val (sessions, pagination) = sessionRepository.find(PaginationRequest(1, 1))

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, sessions.count())
            val session = sessions.first()
            assertEquals(testSession.user, session.user)
            assertEquals(testSession.expiresAt, session.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find last, first page size 1 should return 1 session`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)

            val (sessions, pagination) = sessionRepository.find(
                PaginationRequest(
                    1,
                    1,
                    Sort.DESC
                )
            )

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, sessions.count())
            val session = sessions.first()
            assertEquals(testSession2.user, session.user)
            assertEquals(testSession2.expiresAt, session.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all by ids should return 2 sessions`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            val sessions = sessionRepository.findAllById(listOf(session.id, session2.id))
            assertEquals(2, sessions.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get sessions by user should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            val sessions = sessionRepository.findByUser(testUser)
            assertTrue(sessions.none())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `get sessions should return 1 session`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = testSession.copy(user = testUser)
            sessionRepository.save(testSession)
            val sessions = sessionRepository.findByUser(testUser)
            assertEquals(1, sessions.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should update session`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            val updatedSession = session.copy(
                expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1)
            )
            sessionRepository.save(updatedSession)
            val foundSession = sessionRepository.findById(session.id)
            assertEquals(updatedSession.expiresAt, foundSession!!.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete session by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            sessionRepository.deleteById(session.id)
            assertEquals(0, sessionRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all sessions`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            sessionRepository.deleteAll()
            assertEquals(0, sessionRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all sessions by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            sessionRepository.deleteAllById(listOf(session.id, session2.id))
            assertEquals(0, sessionRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all sessions by entities`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            sessionRepository.deleteAll(listOf(session, session2))
            assertEquals(0, sessionRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete session by entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            sessionRepository.delete(session)
            assertEquals(0, sessionRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            assertEquals(true, sessionRepository.existsById(session.id))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertEquals(false, sessionRepository.existsById((9999).toIdentifier()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return 2`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            assertEquals(2, sessionRepository.count())
        })
    }
}