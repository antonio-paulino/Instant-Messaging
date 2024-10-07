package im.repositories

import im.TestApp
import im.pagination.PaginationRequest
import im.pagination.Sort
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import im.sessions.Session
import im.tokens.AccessToken
import im.user.User
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class AccessTokenRepositoryTest(
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val sessionRepository: SessionRepositoryImpl,
    @Autowired private val accessTokenRepository: AccessTokenRepositoryImpl,
) {

    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testAccessToken: AccessToken
    private lateinit var testAccessToken2: AccessToken
    private lateinit var expiredAccessToken: AccessToken

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        sessionRepository.deleteAll()
        accessTokenRepository.deleteAll()

        testUser = userRepository.save(User(1, "user", "password", "user1@daw.isel.pt"))
        testSession = sessionRepository.save(
            Session(
                user = testUser,
                expiresAt = LocalDateTime.now().plusDays(90),
            )
        )

        testAccessToken = AccessToken(UUID.randomUUID(), testSession, LocalDateTime.now().plusDays(1))
        testAccessToken2 = AccessToken(UUID.randomUUID(), testSession, LocalDateTime.now().plusDays(2))
        expiredAccessToken = AccessToken(UUID.randomUUID(), testSession, LocalDateTime.now().minusDays(1))
    }

    @Test
    @Transactional
    open fun `should save access token`() {
        val savedToken = accessTokenRepository.save(testAccessToken)
        assertNotNull(savedToken.token)
        assertEquals(testAccessToken.session, savedToken.session)
        assertEquals(testAccessToken.expiresAt, savedToken.expiresAt)
    }

    @Test
    @Transactional
    open fun `should find access token by id`() {
        val savedToken = accessTokenRepository.save(testAccessToken)
        val foundToken = accessTokenRepository.findById(savedToken.token)
        assertNotNull(foundToken)
        assertEquals(savedToken.token, foundToken.token)
    }

    @Test
    @Transactional
    open fun `should return null when id does not exist`() {
        val foundToken = accessTokenRepository.findById(UUID.randomUUID())
        assertNull(foundToken)
    }

    @Test
    @Transactional
    open fun `should save multiple access tokens`() {
        val savedToken1 = accessTokenRepository.save(testAccessToken)
        val savedToken2 = accessTokenRepository.save(testAccessToken2)
        val foundTokens = accessTokenRepository.findAll()
        assertEquals(2, foundTokens.count())
        assertTrue(foundTokens.contains(savedToken1))
        assertTrue(foundTokens.contains(savedToken2))
    }

    @Test
    @Transactional
    open fun `should find all access tokens`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)
        val tokens = accessTokenRepository.findAll()
        assertEquals(2, tokens.count())
    }

    @Test
    @Transactional
    open fun `find all should return empty list if no tokens present`() {
        val tokens = accessTokenRepository.findAll()
        assertEquals(0, tokens.count())
    }

    @Test
    @Transactional
    open fun `find first page size 1 should return 1 token`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)

        val (tokens, pagination) = accessTokenRepository.find(PaginationRequest(1, 1))

        assertEquals(2, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.total)
        assertEquals(null, pagination.prevPage)
        assertEquals(2, pagination.nextPage)

        assertEquals(1, tokens.size)
        assertEquals(testAccessToken.token, tokens.first().token)
    }

    @Test
    @Transactional
    open fun `find last page size 1 should return last token`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)
        val (tokens, pagination) = accessTokenRepository.find(PaginationRequest(1, 1, Sort.DESC))

        assertEquals(2, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.total)
        assertEquals(null, pagination.prevPage)
        assertEquals(2, pagination.nextPage)

        assertEquals(1, tokens.size)
        val token = tokens.first()
        assertEquals(testAccessToken2.token, token.token)
    }

    @Test
    @Transactional
    open fun `pagination should return correct tokens for each page`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)

        val (tokens1, pagination1) = accessTokenRepository.find(PaginationRequest(1, 1))
        val (tokens2, pagination2) = accessTokenRepository.find(PaginationRequest(2, 1))

        assertEquals(1, pagination1.currentPage)
        assertEquals(2, pagination2.currentPage)
        assertEquals(2, pagination1.totalPages)
        assertEquals(2, pagination2.totalPages)
        assertEquals(2, pagination1.total)
        assertEquals(2, pagination2.total)
        assertEquals(null, pagination1.prevPage)
        assertEquals(2, pagination1.nextPage)
        assertEquals(1, pagination2.prevPage)
        assertEquals(null, pagination2.nextPage)

        assertEquals(1, tokens1.size)
        assertEquals(1, tokens2.size)
        assertEquals(testAccessToken.token, tokens1.first().token)
        assertEquals(testAccessToken2.token, tokens2.first().token)
    }

    @Test
    @Transactional
    open fun `pagination on empty repository should return empty list`() {
        val tokens = accessTokenRepository.find(PaginationRequest(1, 1)).first
        assertEquals(0, tokens.size)
    }

    @Test
    @Transactional
    open fun `delete session should delete associated access tokens`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)
        sessionRepository.delete(testSession)
        assertEquals(0, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `find all by ids should return matching tokens`() {
        val savedToken1 = accessTokenRepository.save(testAccessToken)
        val savedToken2 = accessTokenRepository.save(testAccessToken2)
        val tokens = accessTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
        assertEquals(2, tokens.count())
    }

    @Test
    @Transactional
    open fun `should delete access token by id`() {
        val savedToken = accessTokenRepository.save(testAccessToken)
        accessTokenRepository.deleteById(savedToken.token)
        assertEquals(0, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete multiple access tokens by ids`() {
        val savedToken1 = accessTokenRepository.save(testAccessToken)
        val savedToken2 = accessTokenRepository.save(testAccessToken2)
        accessTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
        assertEquals(0, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete access token by entity`() {
        val savedToken = accessTokenRepository.save(testAccessToken)
        accessTokenRepository.delete(savedToken)
        assertEquals(0, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all access tokens`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)
        accessTokenRepository.deleteAll()
        assertEquals(0, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true for existing token`() {
        val savedToken = accessTokenRepository.save(testAccessToken)
        assertTrue(accessTokenRepository.existsById(savedToken.token))
    }

    @Test
    @Transactional
    open fun `exists by id should return false for non-existing token`() {
        assertFalse(accessTokenRepository.existsById(UUID.randomUUID()))
    }

    @Test
    @Transactional
    open fun `count should return 2`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.save(testAccessToken2)
        assertEquals(2, accessTokenRepository.count())
    }

    @Test
    @Transactional
    open fun `expired access token should still be retrievable`() {
        accessTokenRepository.save(expiredAccessToken)
        val foundToken = accessTokenRepository.findById(expiredAccessToken.token)
        assertNotNull(foundToken)
        assertEquals(expiredAccessToken.token, foundToken.token)
        assertTrue(foundToken.expiresAt.isBefore(LocalDateTime.now()))
    }

    @Test
    @Transactional
    open fun `deleting non-existing token should not affect repository count`() {
        accessTokenRepository.save(testAccessToken)
        accessTokenRepository.deleteById(UUID.randomUUID())
        assertEquals(1, accessTokenRepository.count())
    }
}
