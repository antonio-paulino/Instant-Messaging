package im

import im.domain.Failure
import im.domain.Success
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import im.services.users.UserError
import im.services.users.UserService
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
abstract class UserServiceTest {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser1: User = User(1L, "testUser1", "testPassword1", "test@daw.isel.pt")
    private var testUser2: User = User(2L, "testUser2", "testPassword2", "test2@daw.isel.pt")

    private var testChannel1: Channel =
        Channel(
            1L,
            "testChannel1",
            owner = testUser1,
            true,
            members = mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER),
        )
    private var testChannel2: Channel =
        Channel(
            2L,
            "testChannel2",
            owner = testUser2,
            true,
            members = mapOf(testUser2 to ChannelRole.OWNER),
        )

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
        transactionManager.run {
            testUser1 = userRepository.save(testUser1)
            testUser2 = userRepository.save(testUser2)
            testChannel1 =
                testChannel1.copy(
                    owner = testUser1,
                    membersLazy = lazy { mapOf(testUser1 to ChannelRole.OWNER, testUser2 to ChannelRole.MEMBER) },
                )
            testChannel2 =
                testChannel2.copy(
                    owner = testUser2,
                    membersLazy = lazy { mapOf(testUser2 to ChannelRole.OWNER, testUser2 to ChannelRole.OWNER) },
                )
            testChannel1 = channelRepository.save(testChannel1)
            testChannel2 = channelRepository.save(testChannel2)
        }
    }

    @Test
    fun `get user by id should return user`() {
        val result = userService.getUserById(testUser1.id)
        assertIs<Success<User>>(result)
        assert(result.value == testUser1)
    }

    @Test
    fun `get user by id user not found`() {
        val result = userService.getUserById(Identifier(1))
        assertIs<Failure<UserError>>(result)
        assert(result.value == UserError.UserNotFound)
    }

    @Test
    fun `get users should return users`() {
        val result = userService.getUsers(null, PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<User>>>(result)
        assert(result.value.items.contains(testUser1))
        assert(result.value.items.contains(testUser2))
    }

    @Test
    fun `get users should return users with name`() {
        val result = userService.getUsers("test", PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<User>>>(result)
        assertTrue(result.value.items.contains(testUser1))
        assertTrue(result.value.items.contains(testUser2))
    }

    @Test
    fun `get users non existing name should return empty`() {
        val result = userService.getUsers("nonExistingName", PaginationRequest(1, 10), SortRequest("id"))
        assertIs<Success<Pagination<User>>>(result)
        assertTrue(result.value.items.isEmpty())
    }

    @Test
    fun `get users paginated should return users`() {
        val result = userService.getUsers(null, PaginationRequest(1, 1), SortRequest("id"))
        assertIs<Success<Pagination<User>>>(result)
        assertEquals(1, result.value.items.size)
        val pagination = result.value.info
        assertEquals(1, pagination!!.currentPage)
        assertEquals(2, pagination.totalPages)
        assertEquals(2, pagination.total)
        assertEquals(2, pagination.nextPage)
        assertNull(pagination.prevPage)
    }

    @Test
    fun `get users last page should return users`() {
        val result = userService.getUsers(null, PaginationRequest(2, 1), SortRequest("id"))
        assertIs<Success<Pagination<User>>>(result)
        assertEquals(1, result.value.items.size)
        val pagination = result.value.info
        assertEquals(2, pagination!!.currentPage)
        assertEquals(2, pagination.totalPages)
        assertEquals(2, pagination.total)
        assertNull(pagination.nextPage)
        assertEquals(1, pagination.prevPage)
    }
}
