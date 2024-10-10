package im.repositories

import im.TestApp
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import im.sessions.Session
import im.tokens.AccessToken
import im.repository.mem.transactions.MemTransactionManager
import im.user.User
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class AccessTokenRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa,
) {

    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testAccessToken: AccessToken
    private lateinit var testAccessToken2: AccessToken
    private lateinit var expiredAccessToken: AccessToken

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
            testUser = userRepository.save(User(1, "user", "password", "user1@daw.isel.pt"))
            testSession = sessionRepository.save(
                Session(
                    user = testUser,
                    expiresAt = LocalDateTime.now().plusDays(90).truncatedTo(ChronoUnit.MILLIS)
                )
            )
            testAccessToken = AccessToken(
                UUID.randomUUID(),
                testSession,
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )
            testAccessToken2 = AccessToken(
                UUID.randomUUID(),
                testSession,
                LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MILLIS)
            )
            expiredAccessToken = AccessToken(
                UUID.randomUUID(),
                testSession,
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save access token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            assertNotNull(savedToken.token)
            assertEquals(testAccessToken.session, savedToken.session)
            assertEquals(testAccessToken.expiresAt, savedToken.expiresAt)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find access token by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            val foundToken = accessTokenRepository.findById(savedToken.token)
            assertNotNull(foundToken)
            assertEquals(savedToken.token, foundToken.token)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null when id does not exist`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundToken = accessTokenRepository.findById(UUID.randomUUID())
            assertNull(foundToken)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save multiple access tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
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
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all access tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            val tokens = accessTokenRepository.findAll()
            assertEquals(2, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all should return empty list if no tokens present`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val tokens = accessTokenRepository.findAll()
            assertEquals(0, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find first page size 1 should return 1 token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

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
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find last page size 1 should return last token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            val (tokens, pagination) = accessTokenRepository.find(
                PaginationRequest(
                    1,
                    1,
                    Sort.DESC
                )
            )

            assertEquals(2, pagination.totalPages)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.total)
            assertEquals(null, pagination.prevPage)
            assertEquals(2, pagination.nextPage)

            assertEquals(1, tokens.size)
            val token = tokens.first()
            assertEquals(testAccessToken2.token, token.token)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `pagination should return correct tokens for each page`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)


            val (tokens1, pagination1) = accessTokenRepository.find(
                PaginationRequest(
                    1,
                    1
                )
            )
            val (tokens2, pagination2) = accessTokenRepository.find(
                PaginationRequest(
                    2,
                    1
                )
            )

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
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `pagination on empty repository should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val tokens = accessTokenRepository.find(PaginationRequest(1, 1)).items
            assertEquals(0, tokens.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `delete session should delete associated access tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            sessionRepository.delete(testSession)
            assertEquals(0, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all by ids should return matching tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)

            val savedToken1 = accessTokenRepository.save(testAccessToken)
            val savedToken2 = accessTokenRepository.save(testAccessToken2)
            val tokens = accessTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(2, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete access token by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            accessTokenRepository.deleteById(savedToken.token)
            assertEquals(0, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete multiple access tokens by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            val savedToken1 = accessTokenRepository.save(testAccessToken)
            val savedToken2 = accessTokenRepository.save(testAccessToken2)
            accessTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(0, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find access tokens should return empty`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            val session = sessionRepository.save(testSession)
            val foundTokens = accessTokenRepository.findBySession(session)
            assertEquals(0, foundTokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find access tokens should return 1 token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            val session = sessionRepository.save(testSession)
            accessTokenRepository.save(testAccessToken.copy(session = session))
            val foundTokens = accessTokenRepository.findBySession(session)
            assertEquals(1, foundTokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete access token by entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            accessTokenRepository.delete(savedToken)
            assertEquals(0, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all access tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            accessTokenRepository.deleteAll()
            assertEquals(0, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true for existing token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            val savedToken = accessTokenRepository.save(testAccessToken)
            assertTrue(accessTokenRepository.existsById(savedToken.token))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false for non-existing token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertFalse(accessTokenRepository.existsById(UUID.randomUUID()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return 2`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            testAccessToken2 = testAccessToken2.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.save(testAccessToken2)
            assertEquals(2, accessTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `expired access token should still be retrievable`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            expiredAccessToken = expiredAccessToken.copy(session = testSession)
            accessTokenRepository.save(expiredAccessToken)
            val foundToken = accessTokenRepository.findById(expiredAccessToken.token)
            assertNotNull(foundToken)
            assertEquals(expiredAccessToken.token, foundToken.token)
            assertTrue(foundToken.expiresAt.isBefore(LocalDateTime.now()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `deleting non-existing token should not affect repository count`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            testUser = userRepository.save(testUser)
            testSession = sessionRepository.save(testSession.copy(user = testUser))
            testAccessToken = testAccessToken.copy(session = testSession)
            accessTokenRepository.save(testAccessToken)
            accessTokenRepository.deleteById(UUID.randomUUID())
            assertEquals(1, accessTokenRepository.count())
        })
    }
}
