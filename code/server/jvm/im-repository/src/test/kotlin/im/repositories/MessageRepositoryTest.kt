package im.repositories

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.messages.Message
import im.domain.user.User
import im.domain.wrappers.identifier.toIdentifier
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
abstract class MessageRepositoryTest {
    private lateinit var testChannel: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testUser: User
    private lateinit var testMessage1: Message
    private lateinit var testMessage2: Message
    private lateinit var testMessage3: Message

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
            testUser = userRepository.save(User(1L, "testUser", "Password123", "user1@daw.isel.pt"))
            testChannel = channelRepository.save(Channel(1L, "General", ChannelRole.MEMBER, testUser, true))
            testChannel2 = channelRepository.save(Channel(2L, "Random", ChannelRole.MEMBER, testUser, true))

            testMessage1 =
                Message(
                    1L,
                    testChannel.id.value,
                    testUser,
                    "Test message 1",
                    LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS),
                )
            testMessage2 =
                Message(
                    testMessage1.id.value + 1,
                    testChannel.id.value,
                    testUser,
                    "Another message",
                    LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MILLIS),
                )
            testMessage3 =
                Message(
                    testMessage2.id.value + 1,
                    testChannel2.id.value,
                    testUser,
                    "Last message",
                    LocalDateTime.now().truncatedTo(
                        ChronoUnit.MILLIS,
                    ),
                )
        }
    }

    @Test
    fun `should return null for non-existent id`() {
        transactionManager.run {
            val foundMessage = messageRepository.findById(999.toIdentifier())
            assertNull(foundMessage)
        }
    }

    @Test
    fun `should return latest messages in channel 1 ordered by createdAt`() {
        transactionManager.run {
            messageRepository.save(testMessage2)
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage3)

            val (latestMessages, pagination) =
                messageRepository.findByChannel(
                    testChannel,
                    PaginationRequest(0, 2),
                    SortRequest("createdAt", Sort.DESC),
                    LocalDateTime.now(),
                )

            assertEquals(1, pagination.currentPage)
            assertNull(pagination.nextPage)
            assertNull(pagination.prevPage)
            assertEquals(2, pagination.total)
            assertEquals(1, pagination.totalPages)
            assertEquals(2, latestMessages.count())
            assertEquals(testMessage2.content, latestMessages.first().content)
            assertEquals(testMessage1.content, latestMessages.last().content)
        }
    }

//    @Test
//    fun `should return latest messages with no count info`() {
//        transactionManager.run {
//            messageRepository.save(testMessage2)
//            messageRepository.save(testMessage1)
//            messageRepository.save(testMessage3)
//
//            val (latestMessages, pagination) =
//                messageRepository.findByChannel(
//                    testChannel,
//                    PaginationRequest(0, 2, false),
//                    SortRequest("createdAt", Sort.DESC),
//                    LocalDateTime.now(),
//                )
//
//            assertEquals(1, pagination.currentPage)
//            assertNull(pagination.nextPage)
//            assertNull(pagination.prevPage)
//            assertNull(pagination.total)
//            assertNull(pagination.totalPages)
//            assertEquals(2, latestMessages.count())
//            assertEquals(testMessage2.content, latestMessages.first().content)
//            assertEquals(testMessage1.content, latestMessages.last().content)
//        }
//    }

    @Test
    fun `should return latest messages with before`() {
        transactionManager.run {
            messageRepository.save(testMessage2)
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage3)

            val (latestMessages, pagination) =
                messageRepository.findByChannel(
                    testChannel,
                    PaginationRequest(0, 2),
                    SortRequest("createdAt", Sort.DESC),
                    testMessage2.createdAt,
                )

            assertEquals(1, pagination.currentPage)
            assertNull(pagination.nextPage)
            assertNull(pagination.prevPage)
            assertEquals(1, pagination.total)
            assertEquals(1, pagination.totalPages)
            assertEquals(1, latestMessages.count())
            assertEquals(testMessage1.content, latestMessages.first().content)
        }
    }

    @Test
    fun `delete channel should delete message`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
            assertEquals(1, messageRepository.count())
        }
        transactionManager.run {
            channelRepository.delete(testChannel)
            channelRepository.flush()
            messageRepository.flush()
            assertNull(channelRepository.findById(testChannel.id))
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `delete user should delete messages`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
            assertEquals(1, messageRepository.count())
            userRepository.delete(testUser)
            userRepository.flush()
            messageRepository.flush()
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `pagination should return correct messages`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            messageRepository.save(testMessage3)

            val (firstPage, pagination1) =
                messageRepository.find(
                    PaginationRequest(0, 2),
                    SortRequest("createdAt", Sort.ASC),
                )
            val (secondPage, pagination2) =
                messageRepository.find(
                    PaginationRequest(2, 2),
                    SortRequest("createdAt", Sort.ASC),
                )

            assertEquals(1, pagination1.currentPage)
            assertEquals(2, pagination1.nextPage)
            assertEquals(3, pagination1.total)
            assertEquals(2, pagination1.totalPages)
            assertNull(pagination1.prevPage)

            assertEquals(2, pagination2.currentPage)
            assertNull(pagination2.nextPage)
            assertEquals(3, pagination2.total)
            assertEquals(2, pagination2.totalPages)
            assertEquals(2, firstPage.size)
            assertEquals(1, secondPage.size)
            assertEquals(testMessage1.content, firstPage.first().content)
            assertEquals(testMessage3.content, secondPage.first().content)
        }
    }

//    @Test
//    fun `pagination should return last message`() {
//        transactionManager.run {
//            messageRepository.save(testMessage1)
//            messageRepository.save(testMessage2)
//            messageRepository.save(testMessage3)
//            val (messages, pagination) =
//                messageRepository.find(
//                    PaginationRequest(2, 2),
//                    SortRequest("createdAt", Sort.ASC),
//                )
//
//            assertEquals(2, pagination.currentPage)
//            assertNull(pagination.nextPage)
//            assertEquals(3, pagination.total)
//            assertEquals(2, pagination.totalPages)
//            assertEquals(1, pagination.prevPage)
//            assertEquals(1, messages.size)
//            assertEquals(testMessage3.content, messages.first().content)
//        }
//    }

    @Test
    fun `pagination on empty repository should return empty list`() {
        transactionManager.run {
            val (messages) = messageRepository.find(PaginationRequest(0, 2), SortRequest("createdAt", Sort.ASC))
            assertTrue(messages.isEmpty())
        }
    }

    @Test
    fun `should save all messages`() {
        transactionManager.run {
            val messages = listOf(testMessage1, testMessage2)
            val savedMessages = messageRepository.saveAll(messages)
            assertEquals(2, savedMessages.size)
        }
    }

    @Test
    fun `should find all by ids`() {
        transactionManager.run {
            val savedMessage1 = messageRepository.save(testMessage1)
            val savedMessage2 = messageRepository.save(testMessage2)
            val messages = messageRepository.findAllById(listOf(savedMessage1.id, savedMessage2.id))
            assertEquals(2, messages.size)
        }
    }

    @Test
    fun `should find by channel and id`() {
        transactionManager.run {
            val savedMessage = messageRepository.save(testMessage1)
            val message = messageRepository.findByChannelAndId(testChannel, savedMessage.id)
            assertEquals(savedMessage.id, message!!.id)
        }
    }

    @Test
    fun `should find all messages`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
        }
        transactionManager.run {
            val messages = messageRepository.findAll()
            assertEquals(1, messages.size)
        }
    }

    @Test
    fun `should update message`() {
        transactionManager.run {
            val savedMessage = messageRepository.save(testMessage1)
            val updatedMessage = savedMessage.copy(content = "Updated message")
            val result = messageRepository.save(updatedMessage)
            assertEquals(updatedMessage.content, result.content)
        }
    }

    @Test
    fun `should delete message by id`() {
        transactionManager.run {
            val savedMessage = messageRepository.save(testMessage1)
            messageRepository.deleteById(savedMessage.id)
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `should delete multiple messages by ids`() {
        transactionManager.run {
            val savedMessage1 = messageRepository.save(testMessage1)
            val savedMessage2 = messageRepository.save(testMessage2)
            messageRepository.deleteAllById(listOf(savedMessage1.id, savedMessage2.id))
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `should delete message entity`() {
        transactionManager.run {
            val savedMessage = messageRepository.save(testMessage1)
            messageRepository.delete(savedMessage)
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `should delete all messages`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            messageRepository.deleteAll()
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `should handle save of empty list`() {
        transactionManager.run {
            val result = messageRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `should handle delete of empty list`() {
        transactionManager.run {
            messageRepository.deleteAll(emptyList())
            assertEquals(0, messageRepository.count())
        }
    }

    @Test
    fun `exists by id should return true for existing message`() {
        transactionManager.run {
            val savedMessage = messageRepository.save(testMessage1)
            assertTrue(messageRepository.existsById(savedMessage.id))
        }
    }

    @Test
    fun `exists by id should return false for non-existing message`() {
        transactionManager.run {
            assertFalse(messageRepository.existsById(testMessage1.id))
        }
    }

    @Test
    fun `count should return correct number of messages`() {
        transactionManager.run {
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            assertEquals(2, messageRepository.count())
        }
    }
}
