package im.repositories

import im.domain.sessions.Session
import im.domain.user.User
import im.domain.wrappers.toIdentifier
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
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
import kotlin.test.assertEquals

private const val SESSION_DURATION_DAYS = 90L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class SessionRepositoryTest {
    private var testUser = User(1, "user", "password", "user1@daw.isel.pt")

    private var testSession: Session =
        Session(
            user = testUser,
            expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS).truncatedTo(ChronoUnit.MILLIS),
        )

    private var testSession2 =
        Session(
            user = testUser,
            expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1).truncatedTo(ChronoUnit.MILLIS),
        )

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
            testUser = userRepository.save(testUser)
            testSession = testSession.copy(user = testUser)
            testSession2 = testSession2.copy(user = testUser)
        }
    }

    @Test
    fun `should save session`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            assertNotNull(session.id)
            assertEquals(testSession.user, session.user)
            assertEquals(testSession.expiresAt, session.expiresAt)
        }
    }

    @Test
    fun `should find session by id`() {
        transactionManager.run {
            val session =
                sessionRepository.save(
                    Session(
                        user = testUser,
                        expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS),
                    ),
                )
            val foundSession = sessionRepository.findById(session.id)
            assertNotNull(foundSession)
            assertEquals(session.user, foundSession!!.user)
            assertEquals(session.expiresAt, foundSession.expiresAt)
        }
    }

    @Test
    fun `should not find by id`() {
        transactionManager.run {
            val foundSession = sessionRepository.findById((9999L).toIdentifier())
            assertNull(foundSession)
        }
    }

    @Test
    fun `should find all sessions`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            val sessions = sessionRepository.findAll()
            assertEquals(2, sessions.count())
        }
    }

    @Test
    fun `find all should return empty list`() {
        transactionManager.run {
            val sessions = sessionRepository.findAll()
            assertEquals(0, sessions.count())
        }
    }

    @Test
    fun `find first, first page size 1 should return 1 session`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            val (sessions, pagination) =
                sessionRepository.find(
                    PaginationRequest(1, 1),
                    SortRequest("expiresAt", Sort.ASC),
                )

            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, sessions.count())
            val session = sessions.first()
            assertEquals(testSession.user, session.user)
            assertEquals(testSession.expiresAt, session.expiresAt)
        }
    }

    @Test
    fun `find last, first page size 1 should return 1 session`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)

            val (sessions, pagination) =
                sessionRepository.find(
                    PaginationRequest(
                        1,
                        1,
                    ),
                    SortRequest("expiresAt", Sort.DESC),
                )

            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, sessions.count())
            val session = sessions.first()
            assertEquals(testSession2.user, session.user)
            assertEquals(testSession2.expiresAt, session.expiresAt)
        }
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            val (sessions, pagination) =
                sessionRepository.find(
                    PaginationRequest(1, 1, getCount = false),
                    SortRequest("expiresAt", Sort.ASC),
                )

            assertNotNull(pagination)
            assertEquals(1, pagination!!.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(null, pagination.prevPage)
            assertNull(pagination.total)
            assertNull(pagination.totalPages)

            assertEquals(1, sessions.count())
            val session = sessions.first()
            assertEquals(testSession.user, session.user)
            assertEquals(testSession.expiresAt, session.expiresAt)
        }
    }

    @Test
    fun `find all by ids should return 2 sessions`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            val sessions = sessionRepository.findAllById(listOf(session.id, session2.id))
            assertEquals(2, sessions.count())
        }
    }

    @Test
    fun `get sessions by user should return empty list`() {
        transactionManager.run {
            userRepository.save(testUser)
            val sessions = sessionRepository.findByUser(testUser)
            assertTrue(sessions.none())
        }
    }

    @Test
    fun `get sessions should return 1 session`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = testSession.copy(user = testUser)
            sessionRepository.save(testSession)
            val sessions = sessionRepository.findByUser(testUser)
            assertEquals(1, sessions.size)
        }
    }

    @Test
    fun `should update session`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            val updatedSession =
                session.copy(
                    expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1),
                )
            sessionRepository.save(updatedSession)
            val foundSession = sessionRepository.findById(session.id)
            assertEquals(updatedSession.expiresAt, foundSession!!.expiresAt)
        }
    }

    @Test
    fun `should delete session by id`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            sessionRepository.deleteById(session.id)
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `should delete all sessions`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            sessionRepository.deleteAll()
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `should delete expired sessions`() {
        transactionManager.run {
            testSession = testSession.copy(expiresAt = LocalDateTime.now().minusDays(1))
            sessionRepository.save(testSession)
            assertEquals(1, sessionRepository.count())
            sessionRepository.deleteExpired()
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `should delete all sessions by ids`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            sessionRepository.deleteAllById(listOf(session.id, session2.id))
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `should delete all sessions by entities`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            val session2 = sessionRepository.save(testSession2)
            sessionRepository.deleteAll(listOf(session, session2))
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `should delete session by entity`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            sessionRepository.delete(session)
            assertEquals(0, sessionRepository.count())
        }
    }

    @Test
    fun `exists by id should return true`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            assertEquals(true, sessionRepository.existsById(session.id))
        }
    }

    @Test
    fun `exists by id should return false`() {
        transactionManager.run {
            assertEquals(false, sessionRepository.existsById((9999).toIdentifier()))
        }
    }

    @Test
    fun `count should return 2`() {
        transactionManager.run {
            sessionRepository.save(testSession)
            sessionRepository.save(testSession2)
            assertEquals(2, sessionRepository.count())
        }
    }
}
