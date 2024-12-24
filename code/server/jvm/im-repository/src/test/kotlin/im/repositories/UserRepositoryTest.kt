package im.repositories

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.sessions.Session
import im.domain.user.User
import im.domain.wrappers.email.toEmail
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.identifier.toIdentifier
import im.domain.wrappers.name.toName
import im.domain.wrappers.password.toPassword
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val SESSION_DURATION_DAYS = 90L

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class UserRepositoryTest {
    private lateinit var testUser: User
    private lateinit var testUser2: User

    private lateinit var testOwnedChannel: Channel
    private lateinit var testInvitedChannel: Channel

    private lateinit var testInvitation: ChannelInvitation

    private lateinit var testSession: Session

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
            testUser = User(1, "user1", "Password123", "user1@daw.isel.pt")
            testUser2 = User(2, "user2", "Password123", "user2@daw.isel.pt")

            testSession =
                Session(
                    user = testUser,
                    expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS).truncatedTo(ChronoUnit.MILLIS),
                )

            testOwnedChannel = Channel(1, "General1", ChannelRole.MEMBER, testUser, true, members = mapOf(testUser2 to ChannelRole.OWNER))
            testInvitedChannel = Channel(2, "General2", ChannelRole.MEMBER, testUser2, true, members = mapOf())

            testInvitation =
                ChannelInvitation(
                    1L,
                    testOwnedChannel,
                    testUser2,
                    testUser,
                    ChannelInvitationStatus.PENDING,
                    ChannelRole.MEMBER,
                    LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MILLIS),
                )
        }
    }

    @Test
    fun `should save and find user by name`() {
        transactionManager.run {
            userRepository.save(testUser)
            val user = userRepository.findByName(testUser.name)
            assertNotNull(user)
            assertEquals(testUser.name, user.name)
        }
    }

    @Test
    fun `save with same name should throw exception`() {
        assertThrows<Exception> {
            transactionManager.run {
                userRepository.save(testUser)
                testUser2 = testUser2.copy(name = testUser.name)
                userRepository.save(testUser2)
            }
        }
    }

    @Test
    fun `save with same email should throw exception`() {
        assertThrows<Exception> {
            transactionManager.run {
                userRepository.save(testUser)
                testUser2 = testUser2.copy(name = "user1".toName(), email = testUser.email)
                userRepository.save(testUser2)
            }
        }
    }

    @Test
    fun `should find user by email`() {
        transactionManager.run {
            userRepository.save(testUser)
            val user = userRepository.findByEmail(testUser.email)
            assertNotNull(user)
            assertEquals(testUser.email, user.email)
        }
    }

    @Test
    fun `should return null when user not found by email`() {
        transactionManager.run {
            val user = userRepository.findByEmail("non-existing@isel.pt".toEmail())
            assertNull(user)
        }
    }

    @Test
    fun `should find user by id`() {
        transactionManager.run {
            val savedUser = userRepository.save(testUser)
            val user = userRepository.findById(savedUser.id)
            assertNotNull(user)
            assertEquals(savedUser.id, user.id)
        }
    }

    @Test
    fun `should return empty when user not found by name`() {
        transactionManager.run {
            val user = userRepository.findByPartialName("non-existing", PaginationRequest(0, 1), SortRequest("id"))
            assertTrue(user.items.isEmpty())
        }
    }

    @Test
    fun `should return null when user not found by id`() {
        transactionManager.run {
            val user = userRepository.findById((9999L).toIdentifier())
            assertNull(user)
        }
    }

    @Test
    fun `should update user information`() {
        transactionManager.run {
            val savedUser = userRepository.save(testUser)
            val updatedUser = savedUser.copy(name = "updatedName".toName(), password = "updatedPassword123".toPassword())
            userRepository.save(updatedUser)
            val user = userRepository.findById(savedUser.id)
            assertNotNull(user)
            assertEquals("updatedName".toName(), user.name)
            assertEquals("updatedPassword123".toPassword(), user.password)
        }
    }

    @Test
    fun `should delete user by id`() {
        transactionManager.run {
            val savedUser = userRepository.save(testUser)
            assertEquals(testUser.name, savedUser.name)
            userRepository.deleteById(savedUser.id)
            val user = userRepository.findById(savedUser.id)
            assertNull(user)
        }
    }

    @Test
    fun `should delete user by entity`() {
        transactionManager.run {
            val savedUser = userRepository.save(testUser)
            assertEquals(testUser.name, savedUser.name)
            userRepository.delete(savedUser)
            val user = userRepository.findById(savedUser.id)
            assertNull(user)
        }
    }

    @Test
    fun `should delete all users`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            userRepository.deleteAll(users)
            assertEquals(0L, userRepository.count())
        }
    }

    @Test
    fun `should delete all users by id`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            val ids = users.map { it.id }
            userRepository.deleteAllById(ids)
            assertEquals(0L, userRepository.count())
        }
    }

    @Test
    fun `should find all users`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val users = userRepository.findAll().toList()
            assertEquals(2, users.size)
        }
    }

    @Test
    fun `should return empty list when no users are found`() {
        transactionManager.run {
            val users = userRepository.findAll()
            assertTrue(users.none())
        }
    }

    @Test
    fun `should not save user with duplicate name`() {
        assertThrows<Exception> {
            transactionManager.run {
                userRepository.save(testUser)
                val user2 = testUser.copy(id = Identifier(999L), password = "password2".toPassword())
                userRepository.save(user2)
            }
        }
    }

    @Test
    fun `should find users by partial name match`() {
        transactionManager.run {
            val user1 = User(name = "john.doe", password = "Password1", email = "user1@daw.isel.pt")
            val user2 = User(name = "jane.doe", password = "Password2", email = "user2@daw.isel.pt")
            val user3 = User(name = "bea.smith", password = "Password3", email = "user3@daw.isel.pt")
            val user4 = User(name = "jane.smith", password = "Password4", email = "user4@daw.isel.pt")
            userRepository.save(user1)
            userRepository.save(user2)
            userRepository.save(user3)
            userRepository.save(user4)
            val users = userRepository.findByPartialName("j", PaginationRequest(0, 2), SortRequest("id"))
            assertEquals(2, users.items.size)
            assertEquals(user1.name, users.items[0].name)
            assertEquals(user2.name, users.items[1].name)
            assertEquals(3, users.info.total)
        }
    }

    @Test
    fun `should find all by id`() {
        transactionManager.run {
            val savedUser1 = userRepository.save(testUser)
            val savedUser2 = userRepository.save(testUser2)
            val users = userRepository.findAllById(listOf(savedUser1.id, savedUser2.id))
            assertEquals(2, users.size)
            assertEquals(testUser.name, users[0].name)
            assertEquals(testUser2.name, users[1].name)
        }
    }

    @Test
    fun `should save all`() {
        transactionManager.run {
            val users = listOf(testUser, testUser2)
            userRepository.saveAll(users)
            assertEquals(2, userRepository.count())
        }
    }

    @Test
    fun `should return empty list when no users are found by partial name match`() {
        transactionManager.run {
            val user1 = User(name = "john.doe", password = "Password1", email = "user1@daw.isel.pt")
            val user2 = User(name = "jane.doe", password = "Password2", email = "user2@daw.isel.pt")
            userRepository.save(user1)
            userRepository.save(user2)
            val users = userRepository.findByPartialName("doe", PaginationRequest(0, 1), SortRequest("id"))
            assertTrue(users.items.isEmpty())
        }
    }

    @Test
    fun `pagination no count`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val (users, pagination) = userRepository.find(PaginationRequest(0, 1, getCount = false), SortRequest("id"))

            assertNotNull(pagination)
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertNull(pagination.prevPage)
            assertNull(pagination.total)
            assertNull(pagination.totalPages)

            assertEquals(1, users.size)
        }
    }

    @Test
    fun `should find user by name and password`() {
        transactionManager.run {
            userRepository.save(testUser)
            val user = userRepository.findByNameAndPassword(testUser.name, testUser.password)
            assertNotNull(user)
            assertEquals(testUser.name, user.name)
            assertEquals(testUser.password, user.password)
        }
    }

    @Test
    fun `should return null when user not found by name and password`() {
        transactionManager.run {
            val user = userRepository.findByNameAndPassword("non-existing".toName(), "Non-existing1".toPassword())
            assertTrue(user == null)
        }
    }

    @Test
    fun `should find user by email and password`() {
        transactionManager.run {
            userRepository.save(testUser)
            val user =
                userRepository.findByEmailAndPassword(
                    testUser.email.value.toEmail(),
                    testUser.password.value.toPassword(),
                )
            assertNotNull(user)
            assertEquals(testUser.email, user.email)
            assertEquals(testUser.password, user.password)
        }
    }

    @Test
    fun `should return null when user not found by email and password`() {
        transactionManager.run {
            val user =
                userRepository.findByEmailAndPassword("non-existing@isel.pt".toEmail(), "Non-existing1".toPassword())
            assertTrue(user == null)
        }
    }

    @Test
    fun `find first, page with size 1 should return user1`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val (users, pagination) = userRepository.find(PaginationRequest(0, 1), SortRequest("id"))
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)
            assertEquals(1, users.size)
            assertEquals(testUser.name, users[0].name)
            assertEquals(testUser.password, users[0].password)
        }
    }

    @Test
    fun `find last, page with size 1 should return user2`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            val (users, pagination) = userRepository.find(PaginationRequest(0, 1), SortRequest("id", Sort.DESC))
            assertEquals(1, pagination.currentPage)
            assertEquals(2, pagination.nextPage)
            assertEquals(2, pagination.total)
            assertEquals(2, pagination.totalPages)
            assertEquals(null, pagination.prevPage)
            assertEquals(1, users.size)
            assertEquals(testUser2.name, users[0].name)
            assertEquals(testUser2.password, users[0].password)
        }
    }

    @Test
    fun `exists by id should return true`() {
        transactionManager.run {
            val savedUser = userRepository.save(testUser)
            assertTrue(userRepository.existsById(savedUser.id))
        }
    }

    @Test
    fun `exists by id should return false`() {
        transactionManager.run {
            userRepository.save(testUser)
            assertFalse(userRepository.existsById((9999L).toIdentifier()))
        }
    }

    @Test
    open fun `count should return 2`() {
        transactionManager.run {
            userRepository.save(testUser)
            userRepository.save(testUser2)
            assertEquals(2, userRepository.count())
        }
    }
}
