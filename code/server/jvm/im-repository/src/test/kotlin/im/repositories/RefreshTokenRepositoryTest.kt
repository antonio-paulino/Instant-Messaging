package im.repositories

import im.TestApp
import im.domain.sessions.Session
import im.domain.tokens.RefreshToken
import im.domain.user.User
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
abstract class RefreshTokenRepositoryTest {
    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testSession2: Session
    private lateinit var testRefreshToken: RefreshToken
    private lateinit var testRefreshToken2: RefreshToken

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
            testUser =
                userRepository.save(
                    User(1, "user", "Password123", "user1@daw.isel.pt"),
                )
            testSession =
                sessionRepository.save(
                    Session(
                        user = testUser,
                        expiresAt = LocalDateTime.now().plusDays(90).truncatedTo(ChronoUnit.MILLIS),
                    ),
                )
            testSession2 =
                sessionRepository.save(
                    Session(
                        user = testUser,
                        expiresAt =
                            LocalDateTime
                                .now()
                                .plusDays(91)
                                .truncatedTo(ChronoUnit.MILLIS),
                    ),
                )

            testRefreshToken = RefreshToken(UUID.randomUUID(), testSession)
            testRefreshToken2 = RefreshToken(UUID.randomUUID(), testSession2)
        }
    }

    @Test
    fun `should save refresh token`() {
        transactionManager.run {
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            assertNotNull(savedToken.token)
            assertEquals(testRefreshToken.session, savedToken.session)
        }
    }

    @Test
    fun `delete session should delete refresh token`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            assertEquals(1, refreshTokenRepository.count())
            sessionRepository.delete(testSession)

            refreshTokenRepository.flush()
            sessionRepository.flush()

            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `should find refresh token by id`() {
        transactionManager.run {
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            val foundToken = refreshTokenRepository.findById(savedToken.token)
            assertNotNull(foundToken)
            assertEquals(savedToken.token, foundToken.token)
            assertEquals(savedToken.session, foundToken.session)
        }
    }

    @Test
    fun `should not find refresh token by id`() {
        transactionManager.run {
            val foundToken = refreshTokenRepository.findById(UUID.randomUUID())
            assertNull(foundToken)
        }
    }

    @Test
    fun `should find all refresh tokens`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val tokens = refreshTokenRepository.findAll()
            assertEquals(2, tokens.count())
        }
    }

    @Test
    fun `find all should return empty list`() {
        transactionManager.run {
            val tokens = refreshTokenRepository.findAll()
            assertEquals(0, tokens.count())
        }
    }

    @Test
    fun `find first, page size 1 should return 1 token`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val (tokens, pagination) = refreshTokenRepository.find(PaginationRequest(0, 1), SortRequest("token", Sort.ASC))

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertNull(pagination.prevPage)

            assertEquals(1, tokens.size)
        }
    }

    @Test
    fun `should save all refresh tokens`() {
        transactionManager.run {
            val savedTokens = refreshTokenRepository.saveAll(listOf(testRefreshToken, testRefreshToken2))
            assertEquals(2, savedTokens.count())
        }
    }

    @Test
    fun `should delete all refresh tokens by entities`() {
        transactionManager.run {
            val savedTokens = refreshTokenRepository.saveAll(listOf(testRefreshToken, testRefreshToken2))
            refreshTokenRepository.deleteAll(savedTokens)
            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val (tokens, pagination) =
                refreshTokenRepository.find(
                    PaginationRequest(0, 1, getCount = false),
                    SortRequest("token", Sort.ASC),
                )

            assertNotNull(pagination)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertNull(pagination.total)
            assertNull(pagination.totalPages)

            assertEquals(1, tokens.size)
        }
    }

    @Test
    fun `find last, page size 1 should return 1 token`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val (tokens, pagination) =
                refreshTokenRepository.find(
                    PaginationRequest(
                        0,
                        1,
                    ),
                    SortRequest("token", Sort.DESC),
                )

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertNull(pagination.prevPage)

            assertEquals(1, tokens.size)
        }
    }

    @Test
    fun `find all by ids should return 2 tokens`() {
        transactionManager.run {
            val savedToken1 = refreshTokenRepository.save(testRefreshToken)
            val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
            val tokens = refreshTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(2, tokens.count())
        }
    }

    @Test
    fun `should delete refresh token by id`() {
        transactionManager.run {
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.deleteById(savedToken.token)
            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `should delete all refresh tokens`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            refreshTokenRepository.deleteAll()
            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `find refresh tokens by session should return empty`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            val foundTokens = refreshTokenRepository.findBySession(session)
            assertEquals(0, foundTokens.count())
        }
    }

    @Test
    fun `find refresh tokens by session should return 1 token`() {
        transactionManager.run {
            val session = sessionRepository.save(testSession)
            testRefreshToken = testRefreshToken.copy(session = session)
            testRefreshToken = refreshTokenRepository.save(testRefreshToken)
            val foundTokens = refreshTokenRepository.findBySession(session)
            assertEquals(1, foundTokens.count())
        }
    }

    @Test
    fun `should delete all refresh tokens by ids`() {
        transactionManager.run {
            val savedToken1 = refreshTokenRepository.save(testRefreshToken)
            val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
            refreshTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `should delete refresh token by entity`() {
        transactionManager.run {
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.delete(savedToken)
            assertEquals(0, refreshTokenRepository.count())
        }
    }

    @Test
    fun `exists by id should return true`() {
        transactionManager.run {
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            assertTrue(refreshTokenRepository.existsById(savedToken.token))
        }
    }

    @Test
    fun `exists by id should return false`() {
        transactionManager.run {
            assertFalse(refreshTokenRepository.existsById(UUID.randomUUID()))
        }
    }

    @Test
    fun `count should return 2`() {
        transactionManager.run {
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            assertEquals(2, refreshTokenRepository.count())
        }
    }
}
