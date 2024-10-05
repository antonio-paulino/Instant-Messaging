package repositories

import channel.Channel
import jakarta.transaction.Transactional
import messages.Message
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ContextConfiguration(classes = [TestAppRepository::class])
open class MessageRepositoryTest(
    @Autowired private val messageRepository: MessageRepositoryImpl,
    @Autowired private val channelRepository: ChannelRepositoryImpl,
    @Autowired private val userRepository: UserRepositoryImpl
) {
    private lateinit var testChannel: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testUser: User
    private lateinit var testMessage1: Message
    private lateinit var testMessage2: Message
    private lateinit var testMessage3: Message

    @BeforeEach
    fun setUp() {
        messageRepository.deleteAll()
        channelRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(User(1L, "testUser", "password", "user1@daw.isel.pt"))

        testChannel = channelRepository.save(Channel(1L, "General", testUser, true))
        testChannel2 = channelRepository.save(Channel(2L, "Random", testUser, true))

        testMessage1 = Message(1L, testChannel, testUser, "Test message 1", LocalDateTime.now().minusDays(1))
        testMessage2 = Message(2L, testChannel, testUser, "Another message", LocalDateTime.now().minusHours(1))
        testMessage3 = Message(3L, testChannel2, testUser, "Last message", LocalDateTime.now())
    }

    @Test
    @Transactional
    open fun `should save a message`() {
        val savedMessage = messageRepository.save(testMessage1)
        assertNotNull(savedMessage.id)
        assertEquals(testMessage1.content, savedMessage.content)
        assertEquals(testMessage1.channel, savedMessage.channel)
        assertEquals(testMessage1.user, savedMessage.user)
    }

    @Test
    @Transactional
    open fun `should find message by id`() {
        val savedMessage = messageRepository.save(testMessage1)
        val foundMessage = messageRepository.findById(savedMessage.id).get()
        assertEquals(savedMessage.id, foundMessage.id)
        assertEquals(savedMessage.content, foundMessage.content)
        assertEquals(savedMessage.user, foundMessage.user)
    }

    @Test
    @Transactional
    open fun `should return empty optional for non-existent id`() {
        val foundMessage = messageRepository.findById(999L)
        assertTrue(foundMessage.isEmpty)
    }

    @Test
    @Transactional
    open fun `should find all messages`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        messageRepository.save(testMessage3)
        val messages = messageRepository.findAll()
        assertEquals(3, messages.count())
    }

    @Test
    @Transactional
    open fun `should return messages by channel`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)

        val messages = messageRepository.findByChannel(testChannel)
        assertEquals(2, messages.count())
    }

    @Test
    @Transactional
    open fun `should return empty list when no messages exist in channel`() {
        val messages = messageRepository.findByChannel(testChannel)
        assertEquals(0, messages.count())
    }

    @Test
    @Transactional
    open fun `should return latest messages in channel 1 ordered by createdAt`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        messageRepository.save(testMessage3)

        val latestMessages = messageRepository.findLatest(testChannel, 0, 3)
        assertEquals(2, latestMessages.count())
        assertEquals(testMessage2.content, latestMessages.first().content)
        assertEquals(testMessage1.content, latestMessages.last().content)
    }

    @Test
    @Transactional
    open fun `delete channel should delete message`() {
        messageRepository.save(testMessage1)
        assertEquals(1, messageRepository.count())
        channelRepository.delete(testChannel)

        channelRepository.flush()
        messageRepository.flush()

        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `delete user should delete messages`() {
        messageRepository.save(testMessage1)
        assertEquals(1, messageRepository.count())
        userRepository.delete(testUser)

        userRepository.flush()
        messageRepository.flush()

        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `pagination should return correct messages`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        messageRepository.save(testMessage3)

        val firstPage = messageRepository.findFirst(0, 2)
        val secondPage = messageRepository.findFirst(1, 2)

        assertEquals(2, firstPage.size)
        assertEquals(1, secondPage.size)
        assertEquals(testMessage1.content, firstPage.first().content)
        assertEquals(testMessage3.content, secondPage.first().content)
    }

    @Test
    @Transactional
    open fun `pagination on empty repository should return empty list`() {
        val messages = messageRepository.findFirst(0, 1)
        assertTrue(messages.isEmpty())
    }

    @Test
    @Transactional
    open fun `should return last messages ordered by id desc`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        messageRepository.save(testMessage3)

        val lastMessages = messageRepository.findLast(0, 2)
        assertEquals(2, lastMessages.size)
        assertEquals(testMessage3.content, lastMessages.first().content)
        assertEquals(testMessage2.content, lastMessages.last().content)
    }

    @Test
    @Transactional
    open fun `should save all messages`() {
        val messages = listOf(testMessage1, testMessage2)
        val savedMessages = messageRepository.saveAll(messages)
        assertEquals(2, savedMessages.size)
    }

    @Test
    @Transactional
    open fun `should update message`() {
        val savedMessage = messageRepository.save(testMessage1)
        val updatedMessage = savedMessage.copy(content = "Updated message")
        val result = messageRepository.save(updatedMessage)
        assertEquals(updatedMessage.content, result.content)
    }

    @Test
    @Transactional
    open fun `should find all messages by ids`() {
        val savedMessage1 = messageRepository.save(testMessage1)
        val savedMessage2 = messageRepository.save(testMessage2)
        val messages = messageRepository.findAllById(listOf(savedMessage1.id, savedMessage2.id))
        assertEquals(2, messages.count())
    }

    @Test
    @Transactional
    open fun `should delete message by id`() {
        val savedMessage = messageRepository.save(testMessage1)
        messageRepository.deleteById(savedMessage.id)
        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete multiple messages by ids`() {
        val savedMessage1 = messageRepository.save(testMessage1)
        val savedMessage2 = messageRepository.save(testMessage2)
        messageRepository.deleteAllById(listOf(savedMessage1.id, savedMessage2.id))
        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete message entity`() {
        val savedMessage = messageRepository.save(testMessage1)
        messageRepository.delete(savedMessage)
        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `should delete all messages`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        messageRepository.deleteAll()
        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `should handle save of empty list`() {
        val result = messageRepository.saveAll(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    @Transactional
    open fun `should handle delete of empty list`() {
        messageRepository.deleteAll(emptyList())
        assertEquals(0, messageRepository.count())
    }

    @Test
    @Transactional
    open fun `exists by id should return true for existing message`() {
        val savedMessage = messageRepository.save(testMessage1)
        assertTrue(messageRepository.existsById(savedMessage.id))
    }

    @Test
    @Transactional
    open fun `exists by id should return false for non-existing message`() {
        assertFalse(messageRepository.existsById(999L))
    }

    @Test
    @Transactional
    open fun `count should return correct number of messages`() {
        messageRepository.save(testMessage1)
        messageRepository.save(testMessage2)
        assertEquals(2, messageRepository.count())
    }
}
