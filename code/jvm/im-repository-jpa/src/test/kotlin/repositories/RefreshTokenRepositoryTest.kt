package repositories

import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import sessions.Session
import tokens.RefreshToken
import user.User
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@ContextConfiguration(classes = [TestAppRepository::class])
open class RefreshTokenRepositoryTest(
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val sessionRepository: SessionRepositoryImpl,
    @Autowired private val refreshTokenRepository: RefreshTokenRepositoryImpl,
) {
    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testSession2: Session
    private lateinit var testRefreshToken: RefreshToken
    private lateinit var testRefreshToken2: RefreshToken

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        sessionRepository.deleteAll()
        refreshTokenRepository.deleteAll()

        testUser = userRepository.save(
            User(1, "user", "password", "user1@daw.isel.pt")
        )
        testSession = sessionRepository.save(
            Session(
                user = testUser,
                expiresAt = LocalDateTime.now().plusDays(90),
            )
        )
        testSession2 = sessionRepository.save(
            Session(
                user = testUser,
                expiresAt = LocalDateTime.now().plusDays(91), // later expiration to test sorting
            )
        )

        testRefreshToken = RefreshToken(UUID.randomUUID(), testSession)
        testRefreshToken2 = RefreshToken(UUID.randomUUID(), testSession2)
    }

    @Test
    @Transactional
    open fun `should save refresh token`() {
        val savedToken = refreshTokenRepository.save(testRefreshToken)
        assertNotNull(savedToken.token)
        assertEquals(testRefreshToken.session, savedToken.session)
    }

    @Test
    @Transactional
    open fun `delete session should delete refresh token`() {
        refreshTokenRepository.save(testRefreshToken)
        assertEquals(1, refreshTokenRepository.count())
        sessionRepository.delete(testSession)

        refreshTokenRepository.flush()
        sessionRepository.flush()

        assertEquals(0, refreshTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should find refresh token by id`() {
        val savedToken = refreshTokenRepository.save(testRefreshToken)
        val foundToken = refreshTokenRepository.findById(savedToken.token)
        assertNotNull(foundToken)
        assertEquals(savedToken.token, foundToken.token)
        assertEquals(savedToken.session, foundToken.session)
    }

    @Test
    @Transactional
    open fun `should not find refresh token by id`() {
        val foundToken = refreshTokenRepository.findById(UUID.randomUUID())
        assertNull(foundToken)
    }

    @Test
    @Transactional
    open fun `should find all refresh tokens`() {
        refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.save(testRefreshToken2)
        val tokens = refreshTokenRepository.findAll()
        assertEquals(2, tokens.count())
    }

    @Test
    @Transactional
    open fun `find all should return empty list`() {
        val tokens = refreshTokenRepository.findAll()
        assertEquals(0, tokens.count())
    }

    @Test
    @Transactional
    open fun `find first, page size 1 should return 1 token`() {
        refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.save(testRefreshToken2)
        val tokens = refreshTokenRepository.findFirst(0, 1)
        assertEquals(1, tokens.size)
        val token = tokens.first()
        assertEquals(testRefreshToken.token, token.token)
    }

    @Test
    @Transactional
    open fun `find last, page size 1 should return 1 token`() {
        refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.save(testRefreshToken2)
        val tokens = refreshTokenRepository.findLast(0, 1)
        assertEquals(1, tokens.size)
        val token = tokens.first()
        assertEquals(testRefreshToken2.token, token.token)
    }

    @Test
    @Transactional
    open fun `find all by ids should return 2 tokens`() {
        val savedToken1 = refreshTokenRepository.save(testRefreshToken)
        val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
        val tokens = refreshTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
        assertEquals(2, tokens.count())
    }

    @Test
    @Transactional
    open fun `should delete refresh token by id`() {
        val savedToken = refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.deleteById(savedToken.token)
        assertEquals(0, refreshTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all refresh tokens`() {
        refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.save(testRefreshToken2)
        refreshTokenRepository.deleteAll()
        assertEquals(0, refreshTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all refresh tokens by ids`() {
        val savedToken1 = refreshTokenRepository.save(testRefreshToken)
        val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
        refreshTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
        assertEquals(0, refreshTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete refresh token by entity`() {
        val savedToken = refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.delete(savedToken)
        assertEquals(0, refreshTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true`() {
        val savedToken = refreshTokenRepository.save(testRefreshToken)
        assertEquals(true, refreshTokenRepository.existsById(savedToken.token))
    }

    @Test
    @Transactional
    open fun `exists by id should return false`() {
        assertEquals(false, refreshTokenRepository.existsById(UUID.randomUUID()))
    }

    @Test
    @Transactional
    open fun `count should return 2`() {
        refreshTokenRepository.save(testRefreshToken)
        refreshTokenRepository.save(testRefreshToken2)
        assertEquals(2, refreshTokenRepository.count())
    }

}
