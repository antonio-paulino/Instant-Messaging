package im.repositories

import im.TestApp
import im.channel.Channel
import im.channel.ChannelRole
import im.invitations.ChannelInvitation
import im.invitations.ChannelInvitationStatus
import jakarta.transaction.Transactional
import im.messages.Message
import im.pagination.PaginationRequest
import im.pagination.Sort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import im.user.User
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ChannelRepositoryTest(
    @Autowired private val userRepository: UserRepositoryImpl,
    @Autowired private val channelRepository: ChannelRepositoryImpl,
    @Autowired private val channelInvitationRepository: ChannelInvitationRepositoryImpl,
    @Autowired private val messageRepository: MessageRepositoryImpl
) {
    private lateinit var testChannel1: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testOwner: User
    private lateinit var testInvitation: ChannelInvitation
    private lateinit var testMessage: Message
    private lateinit var testMember: User

    @BeforeEach
    fun setUp() {
        channelRepository.deleteAll()
        userRepository.deleteAll()
        channelInvitationRepository.deleteAll()
        messageRepository.deleteAll()

        testOwner = userRepository.save(User(1, "Owner", "password", "user1@daw.isel.pt"))
        testMember = userRepository.save(User(2, "Member", "password", "user2@daw.isel.pt"))

        testChannel1 = Channel(
            id = 1L,
            name = "General",
            owner = testOwner,
            isPublic = true,
            createdAt = LocalDateTime.now(),
        )

        testChannel2 = Channel(
            id = 2L,
            name = "Gaming",
            owner = testOwner,
            isPublic = false,
            createdAt = LocalDateTime.now().minusDays(1)
        )

        testInvitation = ChannelInvitation(
            id = 1L,
            channel = testChannel1,
            inviter = testOwner,
            invitee = testOwner,
            status = ChannelInvitationStatus.PENDING,
            role = ChannelRole.MEMBER,
            expiresAt = LocalDateTime.now().plusDays(1)
        )

        testMessage = Message(
            id = 1L,
            channel = testChannel1,
            content = "Hello",
            createdAt = LocalDateTime.now(),
            user = testOwner,
            editedAt = null
        )
    }

    @Test
    @Transactional
    open fun `should save a channel`() {
        val savedChannel = channelRepository.save(testChannel1)
        assertNotNull(savedChannel.id)
        assertEquals(testChannel1.name, savedChannel.name)
        assertEquals(testChannel1.isPublic, savedChannel.isPublic)
        assertEquals(testChannel1.owner, savedChannel.owner)
        assertEquals(testChannel1.createdAt, savedChannel.createdAt)
        assertEquals(testChannel1.members, savedChannel.members)
    }

    @Test
    @Transactional
    open fun `should save multiple channels`() {
        val channels = listOf(testChannel1, testChannel2)
        val savedChannels = channelRepository.saveAll(channels)
        assertEquals(2, savedChannels.size)
    }

    @Test
    @Transactional
    open fun `should find channel by id`() {
        val savedChannel = channelRepository.save(testChannel1)
        val foundChannel = channelRepository.findById(savedChannel.id)
        assertNotNull(foundChannel)
        assertEquals(savedChannel.id, foundChannel?.id)
    }

    @Test
    @Transactional
    open fun `should find channel by name`() {
        channelRepository.save(testChannel1)
        val foundChannel = channelRepository.findByName("General")
        assertNotNull(foundChannel)
        assertEquals("General", foundChannel?.name)
    }

    @Test
    @Transactional
    open fun `should delete on owner delete`() {
        channelRepository.save(testChannel1)
        assertEquals(1, channelRepository.count())
        userRepository.delete(testOwner)

        userRepository.flush()
        channelRepository.flush()

        assertEquals(0, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `should return null for non-existent channel name`() {
        val foundChannel = channelRepository.findByName("NonExistentChannel")
        assertNull(foundChannel)
    }

    @Test
    @Transactional
    open fun `should find channels by partial name`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)
        val foundChannels = channelRepository.findByPartialName("Gen")
        assertEquals(1, foundChannels.count())
        assertEquals(testChannel1.name, foundChannels.first().name)
    }

    @Test
    @Transactional
    open fun `should find all channels`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)
        val channels = channelRepository.findAll()
        assertEquals(2, channels.count())
    }

    @Test
    @Transactional
    open fun `should find first page of channels`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)

        val res = channelRepository.find(PaginationRequest(1, 1))

        val (firstChannels, pagination) = res

        assertEquals(2, pagination.total)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.nextPage)
        assertEquals(2, pagination.totalPages)
        assertEquals(null, pagination.prevPage)

        assertEquals(1, firstChannels.size)
        assertEquals(testChannel1.name, firstChannels.first().name)
    }

    @Test
    @Transactional
    open fun `should find last page of channels ordered by id desc`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)

        val (lastChannels, pagination) = channelRepository.find(PaginationRequest(1, 2, Sort.DESC))

        assertEquals(2, pagination.total)
        assertEquals(1, pagination.currentPage)
        assertEquals(null, pagination.nextPage)
        assertEquals(1, pagination.totalPages)
        assertEquals(null, pagination.prevPage)

        assertEquals(2, lastChannels.size)
        assertEquals(testChannel2.name, lastChannels.first().name)
        assertEquals(testChannel1.name, lastChannels.last().name)
    }


    @Test
    @Transactional
    open fun `should update channel`() {
        val savedChannel = channelRepository.save(testChannel1)
        val updatedChannel = savedChannel.copy(name = "UpdatedName", isPublic = false)
        val result = channelRepository.save(updatedChannel)
        assertEquals("UpdatedName", result.name)
        assertFalse(result.isPublic)
    }

    @Test
    @Transactional
    open fun `get invitations should be empty`() {
        val savedChannel = channelRepository.save(testChannel1)
        val invitations = channelRepository
            .getInvitations(savedChannel, ChannelInvitationStatus.PENDING)
            .toList()
        assertTrue(invitations.isEmpty())
    }

    @Test
    @Transactional
    open fun `get invitations should return one invitation`() {
        val savedChannel = channelRepository.save(testChannel1)
        testInvitation = testInvitation.copy(channel = savedChannel)
        channelInvitationRepository.save(testInvitation)
        val invitations = channelRepository
            .getInvitations(savedChannel, ChannelInvitationStatus.PENDING)
            .toList()
        assertEquals(1, invitations.size)
    }

    @Test
    @Transactional
    open fun `get messages should be empty`() {
        val savedChannel = channelRepository.save(testChannel1)
        val messages = channelRepository
            .getMessages(savedChannel)
            .toList()
        assertTrue(messages.isEmpty())
    }

    @Test
    @Transactional
    open fun `get messages should return one message`() {
        val savedChannel = channelRepository.save(testChannel1)
        testMessage = testMessage.copy(channel = savedChannel)
        messageRepository.save(testMessage)
        val messages = channelRepository
            .getMessages(savedChannel)
            .toList()
        assertEquals(1, messages.size)
    }

    @Test
    @Transactional
    open fun `get member should return empty`() {
        val savedChannel = channelRepository.save(testChannel1)
        val member = channelRepository.getMember(savedChannel, testMember)
        assertNull(member)
    }

    @Test
    @Transactional
    open fun `get member should return member`() {
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = savedChannel.copy(members = savedChannel.members + (testMember to ChannelRole.MEMBER))
        val updatedChannel = channelRepository.save(newChannel)
        val member = channelRepository.getMember(updatedChannel, testMember)
        assertNotNull(member)
        assertEquals(ChannelRole.MEMBER, member!!.second)
        assertEquals(testMember, member.first)
    }

    @Test
    @Transactional
    open fun `should add member to channel with role Member`() {
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = savedChannel.copy(members = savedChannel.members + (testMember to ChannelRole.MEMBER))
        val updatedChannel = channelRepository.save(newChannel)
        assertEquals(2, updatedChannel.members.size) // Owner + Member
        assertEquals(ChannelRole.MEMBER, updatedChannel.members[testMember])
    }

    @Test
    @Transactional
    open fun `should add member to channel with role Guest`() {
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = savedChannel.copy(members = savedChannel.members + (testMember to ChannelRole.GUEST))
        val updatedChannel = channelRepository.save(newChannel)
        assertEquals(2, updatedChannel.members.size) // Owner + Guest
        assertEquals(ChannelRole.GUEST, updatedChannel.members[testMember])
    }

    @Test
    @Transactional
    open fun `should remove member from channel`() {
        val savedChannel = channelRepository.save(testChannel1)

        val newChannel = savedChannel.copy(members = savedChannel.members + (testMember to ChannelRole.MEMBER))
        val updatedChannel = channelRepository.save(newChannel)
        assertEquals(2, updatedChannel.members.size)
        assertEquals(ChannelRole.MEMBER, updatedChannel.members[testMember])
        // Remove Member
        val newChannel2 = updatedChannel.copy(members = updatedChannel.members - testMember)
        val updatedChannel2 = channelRepository.save(newChannel2)

        assertEquals(1, updatedChannel2.members.size) // Owner
    }

    @Test
    @Transactional
    open fun `should delete channel by id`() {
        val savedChannel = channelRepository.save(testChannel1)
        channelRepository.deleteById(savedChannel.id)
        assertEquals(0, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete multiple channels by ids`() {
        val savedChannel1 = channelRepository.save(testChannel1)
        val savedChannel2 = channelRepository.save(testChannel2)
        channelRepository.deleteAllById(listOf(savedChannel1.id, savedChannel2.id))
        assertEquals(0, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete channel entity`() {
        val savedChannel = channelRepository.save(testChannel1)
        channelRepository.delete(savedChannel)
        assertEquals(0, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all channels`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)
        channelRepository.deleteAll()
        assertEquals(0, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true for existing channel`() {
        val savedChannel = channelRepository.save(testChannel1)
        assertTrue(channelRepository.existsById(savedChannel.id))
    }

    @Test
    @Transactional
    open fun `exists by id should return false for non-existing channel`() {
        assertFalse(channelRepository.existsById(999L))
    }

    @Test
    @Transactional
    open fun `count should return correct number of channels`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)
        assertEquals(2, channelRepository.count())
    }

    @Test
    @Transactional
    open fun `should handle save of empty list`() {
        val result = channelRepository.saveAll(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    @Transactional
    open fun `should handle delete of empty list`() {
        channelRepository.deleteAll(emptyList())
        assertEquals(0, channelRepository.count())
    }
}
