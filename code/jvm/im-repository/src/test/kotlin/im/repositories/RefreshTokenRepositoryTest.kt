package im.repositories

import im.TestApp
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import im.sessions.Session
import im.tokens.RefreshToken
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
open class RefreshTokenRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa
) {

    private lateinit var testUser: User
    private lateinit var testSession: Session
    private lateinit var testSession2: Session
    private lateinit var testRefreshToken: RefreshToken
    private lateinit var testRefreshToken2: RefreshToken

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
            userRepository.deleteAll()
            sessionRepository.deleteAll()
            refreshTokenRepository.deleteAll()

            testUser = userRepository.save(
                User(1, "user", "password", "user1@daw.isel.pt")
            )
            testSession = sessionRepository.save(
                Session(
                    user = testUser,
                    expiresAt = LocalDateTime.now().plusDays(90).truncatedTo(ChronoUnit.MILLIS),
                )
            )
            testSession2 = sessionRepository.save(
                Session(
                    user = testUser,
                    expiresAt = LocalDateTime.now().plusDays(91)
                        .truncatedTo(ChronoUnit.MILLIS),
                )
            )

            testRefreshToken = RefreshToken(UUID.randomUUID(), testSession)
            testRefreshToken2 = RefreshToken(UUID.randomUUID(), testSession2)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save refresh token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            assertNotNull(savedToken.token)
            assertEquals(testRefreshToken.session, savedToken.session)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `delete session should delete refresh token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            assertEquals(1, refreshTokenRepository.count())
            sessionRepository.delete(testSession)

            refreshTokenRepository.flush()
            sessionRepository.flush()

            assertEquals(0, refreshTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find refresh token by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            val foundToken = refreshTokenRepository.findById(savedToken.token)
            assertNotNull(foundToken)
            assertEquals(savedToken.token, foundToken.token)
            assertEquals(savedToken.session, foundToken.session)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should not find refresh token by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundToken = refreshTokenRepository.findById(UUID.randomUUID())
            assertNull(foundToken)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all refresh tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val tokens = refreshTokenRepository.findAll()
            assertEquals(2, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val tokens = refreshTokenRepository.findAll()
            assertEquals(0, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find first, page size 1 should return 1 token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val (tokens, pagination) = refreshTokenRepository.find(PaginationRequest(1, 1))

            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)

            assertEquals(1, tokens.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find last, page size 1 should return 1 token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            val (tokens, pagination) = refreshTokenRepository.find(
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

            assertEquals(1, tokens.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find all by ids should return 2 tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken1 = refreshTokenRepository.save(testRefreshToken)
            val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
            val tokens = refreshTokenRepository.findAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(2, tokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete refresh token by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.deleteById(savedToken.token)
            assertEquals(0, refreshTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all refresh tokens`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            refreshTokenRepository.deleteAll()
            assertEquals(0, refreshTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find refresh tokens by session should return empty`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            val foundTokens = refreshTokenRepository.findBySession(session)
            assertEquals(0, foundTokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find refresh tokens by session should return 1 token`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val session = sessionRepository.save(testSession)
            testRefreshToken = testRefreshToken.copy(session = session)
            testRefreshToken = refreshTokenRepository.save(testRefreshToken)
            val foundTokens = refreshTokenRepository.findBySession(session)
            assertEquals(1, foundTokens.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all refresh tokens by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken1 = refreshTokenRepository.save(testRefreshToken)
            val savedToken2 = refreshTokenRepository.save(testRefreshToken2)
            refreshTokenRepository.deleteAllById(listOf(savedToken1.token, savedToken2.token))
            assertEquals(0, refreshTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete refresh token by entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.delete(savedToken)
            assertEquals(0, refreshTokenRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedToken = refreshTokenRepository.save(testRefreshToken)
            assertEquals(true, refreshTokenRepository.existsById(savedToken.token))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertEquals(false, refreshTokenRepository.existsById(UUID.randomUUID()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return 2`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            refreshTokenRepository.save(testRefreshToken)
            refreshTokenRepository.save(testRefreshToken2)
            assertEquals(2, refreshTokenRepository.count())
        })
    }

}
