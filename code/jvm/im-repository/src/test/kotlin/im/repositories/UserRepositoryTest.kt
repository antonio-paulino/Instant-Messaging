package im.repositories

import im.TestApp
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import im.sessions.Session
import im.repository.mem.transactions.MemTransactionManager
import im.user.User
import im.wrappers.Identifier
import im.wrappers.toEmail
import im.wrappers.toIdentifier
import im.wrappers.toName
import im.wrappers.toPassword
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val SESSION_DURATION_DAYS = 90L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class UserRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa,
) {

    private lateinit var testUser: User
    private lateinit var testUser2: User

    private lateinit var testOwnedChannel: Channel
    private lateinit var testInvitedChannel: Channel

    private lateinit var testInvitation: ChannelInvitation

    private lateinit var testSession: Session

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
            testUser = User(1, "user1", "password", "user1@daw.isel.pt")
            testUser2 = User(2, "user2", "password", "user2@daw.isel.pt")

            testSession = Session(
                user = testUser,
                expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS).truncatedTo(ChronoUnit.MILLIS),
            )

            testOwnedChannel = Channel(1, "General1", testUser, true, members = mapOf(testUser2 to ChannelRole.OWNER))
            testInvitedChannel = Channel(2, "General2", testUser2, true, members = mapOf())

            testInvitation = ChannelInvitation(
                1L,
                testOwnedChannel,
                testUser2,
                testUser,
                ChannelInvitationStatus.PENDING,
                ChannelRole.MEMBER,
                LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save and find user by name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            val user = userRepository.findByName(testUser.name)
            assertNotNull(user)
            assertEquals(testUser.name, user.name)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `save with same name should throw exception`(transactionManager: TransactionManager) {
        setup(transactionManager)
        assertThrows<Exception> {
            transactionManager.run({
                userRepository.save(testUser)
                testUser2 = testUser2.copy(name = testUser.name)
                userRepository.save(testUser2)
            })
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `save with same email should throw exception`(transactionManager: TransactionManager) {
        setup(transactionManager)
        assertThrows<Exception> {
            transactionManager.run({
                userRepository.save(testUser)
                testUser2 = testUser2.copy(name = "user1".toName(), email = testUser.email)
                userRepository.save(testUser2)
            })
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find user by email`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            val user = userRepository.findByEmail(testUser.email)
            assertNotNull(user)
            assertEquals(testUser.email, user.email)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null when user not found by email`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user = userRepository.findByEmail("non-existing@isel.pt".toEmail())
            assertNull(user)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find user by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedUser = userRepository.save(testUser)
            val user = userRepository.findById(savedUser.id)
            assertNotNull(user)
            assertEquals(savedUser.id, user.id)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return empty when user not found by name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user = userRepository.findByPartialName("non-existing", PaginationRequest(1, 1))
            assertTrue(user.items.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null when user not found by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user = userRepository.findById((9999L).toIdentifier())
            assertNull(user)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should update user information`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedUser = userRepository.save(testUser)
            val updatedUser = savedUser.copy(name = "updatedName".toName(), password = "updatedPassword".toPassword())
            userRepository.save(updatedUser)
            val user = userRepository.findById(savedUser.id)
            assertNotNull(user)
            assertEquals("updatedName".toName(), user.name)
            assertEquals("updatedPassword".toPassword(), user.password)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete user by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedUser = userRepository.save(testUser)
            assertEquals(testUser.name, savedUser.name)
            userRepository.deleteById(savedUser.id)
            val user = userRepository.findById(savedUser.id)
            assertNull(user)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete user by entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedUser = userRepository.save(testUser)
            assertEquals(testUser.name, savedUser.name)
            userRepository.delete(savedUser)
            val user = userRepository.findById(savedUser.id)
            assertNull(user)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all users`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            userRepository.deleteAll(users)
            assertEquals(0L, userRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all users by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            val ids = users.map { it.id }
            userRepository.deleteAllById(ids)
            assertEquals(0L, userRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find all users`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            assertEquals(2, users.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return empty list when no users are found`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val users = userRepository.findAll()
            assertTrue(users.none())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should not save user with duplicate name`(transactionManager: TransactionManager) {
        setup(transactionManager)
        assertThrows<Exception> {
            transactionManager.run({
                userRepository.save(testUser)
                val user2 = testUser.copy(id = Identifier(999L), password = "password2".toPassword())
                userRepository.save(user2)
            })
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find users by partial name match`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user1 = User(name = "john.doe", password = "password1", email = "user1@daw.isel.pt")
            val user2 = User(name = "jane.doe", password = "password2", email = "user2@daw.isel.pt")
            val user3 = User(name = "bea.smith", password = "password3", email = "user3@daw.isel.pt")
            val user4 = User(name = "jane.smith", password = "password4", email = "user4@daw.isel.pt")
            userRepository.save(user1)
            userRepository.save(user2)
            userRepository.save(user3)
            userRepository.save(user4)
            val users = userRepository.findByPartialName("j", PaginationRequest(1, 2))
            assertEquals(2, users.items.size)
            assertEquals(user1.name, users.items[0].name)
            assertEquals(user2.name, users.items[1].name)
            assertEquals(3, users.info.total)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return empty list when no users are found by partial name match`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user1 = User(name = "john.doe", password = "password1", email = "user1@daw.isel.pt")
            val user2 = User(name = "jane.doe", password = "password2", email = "user2@daw.isel.pt")
            userRepository.save(user1)
            userRepository.save(user2)
            val users = userRepository.findByPartialName("doe", PaginationRequest(1, 1))
            assertTrue(users.items.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find user by name and password`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            val user = userRepository.findByNameAndPassword(testUser.name, testUser.password)
            assertNotNull(user)
            assertEquals(testUser.name, user.name)
            assertEquals(testUser.password, user.password)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null when user not found by name and password`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user = userRepository.findByNameAndPassword("non-existing".toName(), "non-existing".toPassword())
            assertTrue(user == null)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should find user by email and password`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            val user = userRepository.findByEmailAndPassword(
                testUser.email.value.toEmail(),
                testUser.password.value.toPassword()
            )
            assertNotNull(user)
            assertEquals(testUser.email, user.email)
            assertEquals(testUser.password, user.password)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null when user not found by email and password`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val user =
                userRepository.findByEmailAndPassword("non-existing@isel.pt".toEmail(), "non-existing".toPassword())
            assertTrue(user == null)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find first, page with size 1 should return user1`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val (users, pagination) = userRepository.find(PaginationRequest(1, 1))
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)
            assertEquals(1, users.size)
            assertEquals(testUser.name, users[0].name)
            assertEquals(testUser.password, users[0].password)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `find last, page with size 1 should return user2`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val (users, pagination) = userRepository.find(PaginationRequest(1, 1, Sort.DESC))
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)
            assertEquals(1, users.size)
            assertEquals(testUser2.name, users[0].name)
            assertEquals(testUser2.password, users[0].password)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedUser = userRepository.save(testUser)
            assertTrue(userRepository.existsById(savedUser.id))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            assertFalse(userRepository.existsById((9999L).toIdentifier()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return 2`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            userRepository.save(testUser)
            userRepository.save(testUser2)
            assertEquals(2, userRepository.count())
        })
    }
}


