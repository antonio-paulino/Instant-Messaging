package repositories

import channel.Channel
import invitations.ChannelRole
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import user.User
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class ChannelRepositoryTest {

    @Autowired
    private lateinit var channelRepository: ChannelRepositoryImpl

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    private lateinit var testChannel1: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testOwner: User

    @BeforeEach
    fun setUp() {
        channelRepository.deleteAll()
        userRepository.deleteAll()

        testOwner = userRepository.save(User(1, "Owner", "password"))

        testChannel1 = Channel(
            id = 1L,
            name = "General",
            owner = testOwner,
            isPublic = true,
            createdAt = LocalDateTime.now()
        )

        testChannel2 = Channel(
            id = 2L,
            name = "Gaming",
            owner = testOwner,
            isPublic = false,
            createdAt = LocalDateTime.now().minusDays(1)
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
        assertTrue(foundChannel.isPresent)
        assertEquals(savedChannel.id, foundChannel.get().id)
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
        val channels = channelRepository.findFirst(0, 1)
        assertEquals(1, channels.size)
        assertEquals(testChannel1.name, channels.first().name)
    }

    @Test
    @Transactional
    open fun `should find last page of channels ordered by id desc`() {
        channelRepository.save(testChannel1)
        channelRepository.save(testChannel2)

        val lastChannels = channelRepository.findLast(0, 2)
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
    open fun `should add member to channel with role Member`() {
        val user = userRepository.save(User(1, "Member1", "password"))
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = channelRepository.addMember(savedChannel, user, ChannelRole.MEMBER)
        assertEquals(1, newChannel.members.size)
        val userRoles = channelRepository.getUserRoles(newChannel)
        assertEquals(ChannelRole.MEMBER, userRoles[user])
    }

    @Test
    @Transactional
    open fun `should add member to channel with role Owner`() {
        val user = userRepository.save(User(1, "Admin1", "password"))
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = channelRepository.addMember(savedChannel, user, ChannelRole.OWNER)
        assertEquals(1, newChannel.members.size)
        val userRoles = channelRepository.getUserRoles(newChannel)
        assertEquals(ChannelRole.OWNER, userRoles[user])
    }

    @Test
    @Transactional
    open fun `should remove member from channel`() {
        val user = userRepository.save(User(1, "Member 1", "password"))
        val savedChannel = channelRepository.save(testChannel1)
        val newChannel = channelRepository.addMember(savedChannel, user, ChannelRole.MEMBER)
        val updatedChannel = channelRepository.removeMember(newChannel, user)
        assertEquals(0, updatedChannel.members.size)
        assertEquals(0, updatedChannel.members.size)
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
