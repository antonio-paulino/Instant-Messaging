package repositories

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import sessions.SESSION_DURATION_DAYS
import sessions.Session
import tokens.AccessToken
import tokens.RefreshToken
import user.User
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest
@ContextConfiguration(classes = [TestAppRepository::class])
open class SessionRepositoryTest(
    @Autowired private val sessionRepository: SessionRepositoryImpl,
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val accessTokenRepository: AccessTokenRepositoryImpl,
    @Autowired private val refreshTokenRepository: RefreshTokenRepositoryImpl,
) {

    private var testUser = User(1, "user", "password", "user1@daw.isel.pt")

    private var testSession: Session = Session(
        user = testUser,
        expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS),
    )

    private var testSession2 = Session(
        user = testUser,
        expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1),
    )

    private val testAccessToken = AccessToken(UUID.randomUUID(), testSession, LocalDateTime.now().plusDays(1))

    private val testRefreshToken = RefreshToken(UUID.randomUUID(), testSession)

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        sessionRepository.deleteAll()
        accessTokenRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        testUser = userRepository.save(testUser)
        testSession = testSession.copy(user = testUser)
        testSession2 = testSession2.copy(user = testUser)

    }

    @Test
    @Transactional
    open fun `should save session`() {
        val session = sessionRepository.save(testSession)
        assertNotNull(session.id)
        assertEquals(testSession.user, session.user)
        assertEquals(testSession.expiresAt, session.expiresAt)
    }

    @Test
    @Transactional
    open fun `should find session by id`() {
        val session = sessionRepository.save(
            Session(
                user = testUser,
                expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS),
            )
        )
        val foundSession = sessionRepository.findById(session.id).get()
        assertNotNull(foundSession)
        assertEquals(session.user, foundSession.user)
        assertEquals(session.expiresAt, foundSession.expiresAt)
    }

    @Test
    @Transactional
    open fun `should not find by id`() {
        val foundSession = sessionRepository.findById(9999)
        assertEquals(false, foundSession.isPresent)
    }

    @Test
    @Transactional
    open fun `should find all sessions`() {
        sessionRepository.save(testSession)
        sessionRepository.save(testSession2)
        val sessions = sessionRepository.findAll()
        assertEquals(2, sessions.count())
    }

    @Test
    @Transactional
    open fun `find all should return empty list`() {
        val sessions = sessionRepository.findAll()
        assertEquals(0, sessions.count())
    }

    @Test
    @Transactional
    open fun `find first, first page size 1 should return 1 session`() {
        sessionRepository.save(testSession)
        sessionRepository.save(testSession2)
        val sessions = sessionRepository.findFirst(0, 1)
        assertEquals(1, sessions.count())
        val session = sessions.first()
        assertEquals(testSession.user, session.user)
        assertEquals(testSession.expiresAt, session.expiresAt)
    }

    @Test
    @Transactional
    open fun `find last, first page size 1 should return 1 session`() {
        sessionRepository.save(testSession)
        sessionRepository.save(testSession2)
        val sessions = sessionRepository.findLast(0, 1)
        assertEquals(1, sessions.count())
        val session = sessions.first()
        assertEquals(testSession2.user, session.user)
        assertEquals(testSession2.expiresAt, session.expiresAt)
    }

    @Test
    @Transactional
    open fun `find all by ids should return 2 sessions`() {
        val session = sessionRepository.save(testSession)
        val session2 = sessionRepository.save(testSession2)
        val sessions = sessionRepository.findAllById(listOf(session.id, session2.id))
        assertEquals(2, sessions.count())
    }

    @Test
    @Transactional
    open fun `find access tokens should return empty`() {
        val session = sessionRepository.save(testSession)
        val foundTokens = sessionRepository.getAccessTokens(session)
        assertEquals(0, foundTokens.count())
    }

    @Test
    @Transactional
    open fun `find access tokens should return 1 token`() {
        val session = sessionRepository.save(testSession)
        accessTokenRepository.save(testAccessToken.copy(session = session))
        val foundTokens = sessionRepository.getAccessTokens(session)
        assertEquals(1, foundTokens.count())
    }

    @Test
    @Transactional
    open fun `find refresh tokens should return empty`() {
        val session = sessionRepository.save(testSession)
        val foundTokens = sessionRepository.getRefreshTokens(session)
        assertEquals(0, foundTokens.count())
    }

    @Test
    @Transactional
    open fun `find refresh tokens should return 1 token`() {
        val session = sessionRepository.save(testSession)
        refreshTokenRepository.save(testRefreshToken.copy(session = session))
        val foundTokens = sessionRepository.getRefreshTokens(session)
        assertEquals(1, foundTokens.count())
    }

    @Test
    @Transactional
    open fun `should update session`() {
        val session = sessionRepository.save(testSession)
        val updatedSession = session.copy(
            expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS + 1)
        )
        sessionRepository.save(updatedSession)
        val foundSession = sessionRepository.findById(session.id).get()
        assertEquals(updatedSession.expiresAt, foundSession.expiresAt)
    }

    @Test
    @Transactional
    open fun `should delete session by id`() {
        val session = sessionRepository.save(testSession)
        sessionRepository.deleteById(session.id)
        assertEquals(0, sessionRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all sessions`() {
        sessionRepository.save(testSession)
        sessionRepository.save(testSession2)
        sessionRepository.deleteAll()
        assertEquals(0, sessionRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all sessions by ids`() {
        val session = sessionRepository.save(testSession)
        val session2 = sessionRepository.save(testSession2)
        sessionRepository.deleteAllById(listOf(session.id, session2.id))
        assertEquals(0, sessionRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all sessions by entities`() {
        val session = sessionRepository.save(testSession)
        val session2 = sessionRepository.save(testSession2)
        sessionRepository.deleteAll(listOf(session, session2))
        assertEquals(0, sessionRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete session by entity`() {
        val session = sessionRepository.save(testSession)
        sessionRepository.delete(session)
        assertEquals(0, sessionRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true`() {
        val session = sessionRepository.save(testSession)
        assertEquals(true, sessionRepository.existsById(session.id))
    }

    @Test
    @Transactional
    open fun `exists by id should return false`() {
        assertEquals(false, sessionRepository.existsById(9999))
    }

    @Test
    @Transactional
    open fun `count should return 2`() {
        sessionRepository.save(testSession)
        sessionRepository.save(testSession2)
        assertEquals(2, sessionRepository.count())
    }

}