package repositories

import channel.Channel
import channel.ChannelRole
import invitations.ChannelInvitation
import invitations.ChannelInvitationStatus
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ContextConfiguration
import sessions.Session
import user.User
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val SESSION_DURATION_DAYS = 90L


@SpringBootTest
@ContextConfiguration(classes = [TestAppRepository::class])
open class UserRepositoryTest(
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val sessionRepository: SessionRepositoryImpl,
    @Autowired private val channelRepository: ChannelRepositoryImpl,
    @Autowired private val channelInvitationRepository: ChannelInvitationRepositoryImpl,
) {

    private lateinit var testUser: User
    private lateinit var testUser2: User

    private lateinit var testOwnedChannel: Channel
    private lateinit var testInvitedChannel: Channel

    private lateinit var testInvitation: ChannelInvitation

    private lateinit var testSession: Session

    @BeforeEach
    open fun setUp() {
        userRepository.deleteAll()
        sessionRepository.deleteAll()
        channelRepository.deleteAll()
        channelInvitationRepository.deleteAll()

        testUser = User(1, "user1", "password", "user1@daw.isel.pt")
        testUser2 = User(2, "user2", "password", "user2@daw.isel.pt")

        testSession = Session(
            user = testUser,
            expiresAt = LocalDateTime.now().plusDays(SESSION_DURATION_DAYS),
        )

        testOwnedChannel = Channel(1, "General", testUser, true, members = mapOf())
        testInvitedChannel = Channel(2, "General", testUser2, true, members = mapOf())

        testInvitation = ChannelInvitation(
            1L,
            testOwnedChannel,
            testUser2,
            testUser,
            ChannelInvitationStatus.PENDING,
            ChannelRole.MEMBER,
            LocalDateTime.now().plusDays(1)
        )

    }

    @Test
    @Transactional
    open fun `should save and find user by name`() {
        userRepository.save(testUser)
        val user = userRepository.findByName(testUser.name)
        assertNotNull(user)
        assertEquals(testUser.name, user.name)
    }

    @Test
    @Transactional
    open fun `should find user by email`() {
        userRepository.save(testUser)
        val user = userRepository.findByEmail(testUser.email)
        assertNotNull(user)
        assertEquals(testUser.email, user.email)
    }

    @Test
    @Transactional
    open fun `should return null when user not found by email`() {
        val user = userRepository.findByEmail("non-existing")
        assertNull(user)
    }


    @Test
    @Transactional
    open fun `should find user by id`() {
        val savedUser = userRepository.save(testUser)
        val user = userRepository.findById(savedUser.id)
        assertNotNull(user)
        assertEquals(savedUser.id, user.id)
    }

    @Test
    @Transactional
    open fun `should return empty when user not found by name`() {
        val user = userRepository.findByPartialName("non-existing")
        assertTrue(user.isEmpty())
    }

    @Test
    @Transactional
    open fun `should return null when user not found by id`() {
        val user = userRepository.findById(9999L)
        assertNull(user)
    }

    @Test
    @Transactional
    open fun `should update user information`() {
        val savedUser = userRepository.save(testUser)
        val updatedUser = savedUser.copy(name = "updatedName", password = "updatedPassword")
        userRepository.save(updatedUser)

        val user = userRepository.findById(savedUser.id)
        assertNotNull(user)
        assertEquals("updatedName", user.name)
        assertEquals("updatedPassword", user.password)
    }

    @Test
    @Transactional
    open fun `should delete user by id`() {
        val savedUser = userRepository.save(testUser)

        assertEquals(testUser.name, savedUser.name)

        userRepository.deleteById(savedUser.id)

        val user = userRepository.findById(savedUser.id)
        assertNull(user)
    }

    @Test
    @Transactional
    open fun `should delete user by entity`() {
        val savedUser = userRepository.save(testUser)

        assertEquals(testUser.name, savedUser.name)

        userRepository.delete(savedUser)

        val user = userRepository.findById(savedUser.id)
        assertNull(user)
    }

    @Test
    @Transactional
    open fun `should delete all users`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)
        val users = userRepository.findAll().toList()
        userRepository.deleteAll(users)
        assertEquals(0L, userRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all users by id`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)

        val users = userRepository.findAll().toList()
        val ids = users.map { it.id }

        userRepository.deleteAllById(ids)
        assertEquals(0L, userRepository.count())
    }

    @Test
    @Transactional
    open fun `should find all users`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)
        val users = userRepository.findAll().toList()
        assertEquals(2, users.size)
    }

    @Test
    @Transactional
    open fun `should return empty list when no users are found`() {
        val users = userRepository.findAll()
        assertTrue(users.none())
    }

    @Test
    @Transactional
    open fun `get sessions should return empty list`() {
        userRepository.save(testUser)
        val sessions = userRepository.getSessions(testUser)
        assertTrue(sessions.none())
    }

    @Test
    @Transactional
    open fun `get sessions should return 1 session`() {
        testUser = userRepository.save(testUser)
        testSession = testSession.copy(user = testUser)
        sessionRepository.save(testSession)
        val sessions = userRepository.getSessions(testUser)
        assertEquals(1, sessions.size)
    }

    @Test
    @Transactional
    open fun `get joined channels should return empty list`() {
        testUser = userRepository.save(testUser)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser)

        channelRepository.save(testOwnedChannel)
        channelRepository.save(testInvitedChannel)

        val channels = userRepository.getJoinedChannels(testUser)
        assertTrue(channels.none())
    }

    @Test
    @Transactional
    open fun `get joined channels should return 1 channel`() {
        testUser = userRepository.save(testUser)
        testUser2 = userRepository.save(testUser2)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser2, members = mapOf(testUser to ChannelRole.MEMBER))

        channelRepository.save(testOwnedChannel)
        testInvitedChannel = channelRepository.save(testInvitedChannel)

        val channels = userRepository.getJoinedChannels(testUser)
        assertEquals(1, channels.size)
        assertEquals(ChannelRole.MEMBER, channels[testInvitedChannel])
    }

    @Test
    @Transactional
    open fun `get owned channels should be empty`() {
        testUser = userRepository.save(testUser)
        testUser2 = userRepository.save(testUser2)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser)

        channelRepository.save(testOwnedChannel)
        channelRepository.save(testInvitedChannel)

        val channels = userRepository.getOwnedChannels(testUser2)
        assertTrue(channels.none())
    }

    @Test
    @Transactional
    open fun `get owned channels should return 1 channel`() {
        testUser = userRepository.save(testUser)
        testUser2 = userRepository.save(testUser2)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser2)

        testOwnedChannel = channelRepository.save(testOwnedChannel)
        testInvitedChannel = channelRepository.save(testInvitedChannel)

        val channels = userRepository.getOwnedChannels(testUser)
        assertEquals(1, channels.size)
        assertEquals(testOwnedChannel, channels[0])
    }

    @Test
    @Transactional
    open fun `get invitations should return empty list`() {
        testUser = userRepository.save(testUser)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser)

        channelRepository.save(testOwnedChannel)
        channelRepository.save(testInvitedChannel)

        val channels = userRepository.getInvitations(testUser)
        assertTrue(channels.none())
    }

    @Test
    @Transactional
    open fun `get invitations should return 1 invitation`() {
        testUser = userRepository.save(testUser)
        testUser2 = userRepository.save(testUser2)

        testOwnedChannel = testOwnedChannel.copy(owner = testUser)
        testInvitedChannel = testInvitedChannel.copy(owner = testUser2)

        channelRepository.save(testOwnedChannel)
        testInvitedChannel = channelRepository.save(testInvitedChannel)

        testInvitation = testInvitation.copy(
            inviter = testUser,
            invitee = testUser2,
            channel = testInvitedChannel
        )

        channelInvitationRepository.save(testInvitation)

        val channels = userRepository.getInvitations(testUser2)
        assertEquals(1, channels.size)
    }

    @Test
    @Transactional
    open fun `get owned channels should return empty list`() {
        userRepository.save(testUser)
        val channels = userRepository.getOwnedChannels(testUser)
        assertTrue(channels.none())
    }

    @Test
    @Transactional
    open fun `should not save user with duplicate name`() {
        userRepository.save(testUser)

        val user2 = testUser.copy(password = "password2")

        assertThrows<DataIntegrityViolationException> {
            userRepository.save(user2)
        }
    }

    @Test
    @Transactional
    open fun `should find users by partial name match`() {
        val user1 = User(
            name = "john.doe",
            password = "password1",
            email = "user1@daw.isel.pt"
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            email = "user2@daw.isel.pt"
        )
        userRepository.save(user1)
        userRepository.save(user2)
        val users = userRepository.findByPartialName("j")
        assertEquals(2, users.size)
    }

    @Test
    @Transactional
    open fun `should return empty list when no users are found by partial name match`() {
        val user1 = User(
            name = "john.doe",
            password = "password1",
            email = "user1@daw.isel.pt"
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            email = "user2@daw.isel.pt"
        )
        userRepository.save(user1)
        userRepository.save(user2)
        val users = userRepository.findByPartialName("doe")
        assertTrue(users.none())
    }

    @Test
    @Transactional
    open fun `should find user by name and password`() {
        userRepository.save(testUser)
        val user = userRepository.findByNameAndPassword(testUser.name, testUser.password)
        assertNotNull(user)
        assertEquals(testUser.name, user.name)
        assertEquals(testUser.password, user.password)
    }

    @Test
    @Transactional
    open fun `should return null when user not found by name and password`() {
        val user = userRepository.findByNameAndPassword("non-existing", "non-existing")
        assertTrue(user == null)
    }

    @Test
    @Transactional
    open fun `should find user by email and password`() {
        userRepository.save(testUser)
        val user = userRepository.findByEmailAndPassword(testUser.email, testUser.password)
        assertNotNull(user)
        assertEquals(testUser.email, user.email)
        assertEquals(testUser.password, user.password)
    }

    @Test
    @Transactional
    open fun `should return null when user not found by email and password`() {
        val user = userRepository.findByEmailAndPassword("non-existing", "non-existing")
        assertTrue(user == null)
    }

    @Test
    @Transactional
    open fun `find first, page with size 1 should return user1`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)
        val users = userRepository.findFirst(0, 1)
        assertEquals(1, users.size)
        assertEquals(testUser.name, users[0].name)
        assertEquals(testUser.password, users[0].password)
    }

    @Test
    @Transactional
    open fun `find last, page with size 1 should return user2`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)

        val users = userRepository.findLast(0, 1)

        assertEquals(1, users.size)
        assertEquals(testUser2.name, users[0].name)
        assertEquals(testUser2.password, users[0].password)
    }

    @Test
    @Transactional
    open fun `exists by id should return true`() {
        val savedUser = userRepository.save(testUser)
        assertTrue(userRepository.existsById(savedUser.id))
    }

    @Test
    @Transactional
    open fun `exists by id should return false`() {
        userRepository.save(testUser)
        assertFalse(userRepository.existsById(9999L))
    }

    @Test
    @Transactional
    open fun `count should return 2`() {
        userRepository.save(testUser)
        userRepository.save(testUser2)
        assertEquals(2, userRepository.count())
    }
}


