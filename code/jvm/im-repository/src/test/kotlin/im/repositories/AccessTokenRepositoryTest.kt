package im.repositories

import im.domain.sessions.Session
import im.domain.tokens.AccessToken
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
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class AccessTokenRepositoryTest {
    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testAccessToken: AccessToken
    private lateinit var testAccessToken2: AccessToken
    private lateinit var expiredAccessToken: AccessToken

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
            testUser = userRepository.save(User(1, "user", "Password123", "user1@daw.isel.pt"))
            testSession =
                sessionRepository.save(
                    Session(
                        user = testUser,
                        expiresAt = LocalDateTime.now().plusDays(90).truncatedTo(ChronoUnit.MILLIS),
                    ),
                )
            testAccessToken =
                AccessToken(
                    UUID.randomUUID(),
                    testSession,
                    LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                )
            testAccessToken2 =
                AccessToken(
                    UUID.randomUUID(),
                    testSession,
                    LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS),
                )
            expiredAccessToken =
                AccessToken(
                    UUID.randomUUID(),
                    testSession,
                    LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                )
        }
    }

    @Test
    open fun `should save access token`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            assertNotNull(savedToken.token)
            assertEquals(testAccessToken.session, savedToken.session)
            assertEquals(testAccessToken.expiresAt, savedToken.expiresAt)
        }
    }

    @Test
    open fun `should find access token by id`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            val foundToken = accessTokenRepository.findById(savedToken.token)
            assertNotNull(foundToken)
            assertEquals(savedToken.token, foundToken.token)
        }
    }

    @Test
    open fun `should return null when id does not exist`() {
        transactionManager.run {
            val foundToken = accessTokenRepository.findById(UUID.randomUUID())
            assertNull(foundToken)
        }
    }

    @Test
    open fun `should save multiple access tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            val savedToken1 = accessTokenRepository.save(testAccessToken)
            val savedToken2 = accessTokenRepository.save(testAccessToken2)
            val foundTokens = accessTokenRepository.findAll()
            assertEquals(2, foundTokens.count())
            assertTrue(foundTokens.contains(savedToken1))
            assertTrue(foundTokens.contains(savedToken2))
        }
    }

    @Test
    open fun `should find all access tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            val tokens = accessTokenRepository.findAll()
            assertEquals(2, tokens.count())
        }
    }

    @Test
    open fun `find all should return empty list if no tokens present`() {
        transactionManager.run {
            val tokens = accessTokenRepository.findAll()
            assertEquals(0, tokens.count())
        }
    }

    @Test
    open fun `find first page size 1 should return 1 token`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)

            val (tokens, pagination) =
                accessTokenRepository.find(
                    PaginationRequest(1, 1),
                    SortRequest("expiresAt", Sort.ASC),
                )

            assertEquals(2, pagination!!.totalPages)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.total)
            assertEquals(null, pagination.prevPage)
            assertEquals(2, pagination.nextPage)

            assertEquals(1, tokens.size)
            assertEquals(testAccessToken.token, tokens.first().token)
        }
    }

    @Test
    open fun `find last page size 1 should return last token`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            val (tokens, pagination) =
                accessTokenRepository.find(
                    PaginationRequest(
                        1,
                        1,
                    ),
                    SortRequest("expiresAt", Sort.DESC),
                )

            assertEquals(2, pagination!!.totalPages)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.total)
            assertEquals(null, pagination.prevPage)
            assertEquals(2, pagination.nextPage)

            assertEquals(1, tokens.size)
            val token = tokens.first()
            assertEquals(testAccessToken2.token, token.token)
        }
    }

    @Test
    open fun `pagination should return correct tokens for each page`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)

            val (tokens1, pagination1) =
                accessTokenRepository.find(
                    PaginationRequest(
                        1,
                        1,
                    ),
                    SortRequest("expiresAt", Sort.ASC),
                )
            val (tokens2, pagination2) =
                accessTokenRepository.find(
                    PaginationRequest(
                        2,
                        1,
                    ),
                    SortRequest("expiresAt", Sort.ASC),
                )

            assertEquals(1, pagination1!!.currentPage)
            assertEquals(2, pagination2!!.currentPage)
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
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)

            val (tokens1, pagination1) =
                accessTokenRepository.find(
                    PaginationRequest(
                        1,
                        1,
                        getCount = false,
                    ),
                    SortRequest("expiresAt", Sort.ASC),
                )
            val (tokens2, pagination2) =
                accessTokenRepository.find(
                    PaginationRequest(
                        2,
                        1,
                        getCount = false,
                    ),
                    SortRequest("expiresAt", Sort.ASC),
                )

            assertEquals(1, tokens1.size)
            assertEquals(1, tokens2.size)
            assertEquals(testAccessToken.token, tokens1.first().token)
            assertEquals(testAccessToken2.token, tokens2.first().token)
            assertNotNull(pagination1)
            assertNotNull(pagination2)
            assertEquals(1, pagination1.currentPage)
            assertEquals(2, pagination2.currentPage)
            assertNull(pagination1.totalPages)
            assertNull(pagination2.totalPages)
            assertNull(pagination1.total)
            assertNull(pagination2.total)
        }
    }

    @Test
    open fun `pagination on empty repository should return empty list`() {
        transactionManager.run {
            val tokens = accessTokenRepository.find(PaginationRequest(1, 1), SortRequest("expiresAt", Sort.ASC)).items
            assertEquals(0, tokens.size)
        }
    }

    @Test
    open fun `delete session should delete associated access tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            sessionRepository.delete(testSession)
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `find all by ids should return matching tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            val savedToken1 = accessTokenRepository.save(testAccessToken)
            val savedToken2 = accessTokenRepository.save(testAccessToken2)
            val tokens = accessTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(2, tokens.count())
        }
    }

    @Test
    open fun `should delete access token by id`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            accessTokenRepository.deleteById(savedToken.token)
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `should delete multiple access tokens by ids`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            val savedToken1 = accessTokenRepository.save(testAccessToken)
            val savedToken2 = accessTokenRepository.save(testAccessToken2)
            accessTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `find access tokens should return empty`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            val session = sessionRepository.save(testSession)
            val foundTokens = accessTokenRepository.findBySession(session)
            assertEquals(0, foundTokens.count())
        }
    }

    @Test
    open fun `find access tokens should return 1 token`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            val session = sessionRepository.save(testSession)
            accessTokenRepository.save(testAccessToken.copy(session = session))
            val foundTokens = accessTokenRepository.findBySession(session)
            assertEquals(1, foundTokens.count())
        }
    }

    @Test
    open fun `should delete access token by entity`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            accessTokenRepository.delete(savedToken)
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `should delete all access tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            accessTokenRepository.deleteAll()
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `should delete expired access tokens`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            expiredAccessToken = expiredAccessToken.copy(session = testSession)
            accessTokenRepository.save(expiredAccessToken)
            accessTokenRepository.deleteExpired()
            assertEquals(0, accessTokenRepository.count())
        }
    }

    @Test
    open fun `exists by id should return true for existing token`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            assertTrue(accessTokenRepository.existsById(savedToken.token))
        }
    }

    @Test
    open fun `exists by id should return false for non-existing token`() {
        transactionManager.run {
            assertFalse(accessTokenRepository.existsById(UUID.randomUUID()))
        }
    }

    @Test
    open fun `count should return 2`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            assertEquals(2, accessTokenRepository.count())
        }
    }

    @Test
    open fun `expired access token should still be retrievable`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            expiredAccessToken = expiredAccessToken.copy(session = testSession)
            accessTokenRepository.save(expiredAccessToken)
            val foundToken = accessTokenRepository.findById(expiredAccessToken.token)
            assertNotNull(foundToken)
            assertEquals(expiredAccessToken.token, foundToken.token)
            assertTrue(foundToken.expiresAt.isBefore(LocalDateTime.now()))
        }
    }

    @Test
    open fun `deleting non-existing token should not affect repository count`() {
        transactionManager.run {
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.deleteById(UUID.randomUUID())
            assertEquals(1, accessTokenRepository.count())
        }
    }
}
