package im

import im.domain.Failure
import im.domain.Success
import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.messages.Message
import im.domain.user.User
import im.domain.wrappers.identifier.toIdentifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import im.services.messages.MessageError
import im.services.messages.MessageService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
abstract class MessageServiceTest {
    @Autowired
    private lateinit var messageService: MessageService

    @Autowired
    private lateinit var transactionManager: TransactionManager

    private var testUser = User(1L, "testUser", "Password123", "iseldaw@isel.pt")
    private var testUser2 = User(2L, "testUser2", "Password123", "iseldaw2@isel.pt")
    private var testUser3 = User(3L, "testUser3", "Password123", "iseldaw3@isel.pt")
    private var testChannel = Channel(1L, "testChannel", ChannelRole.MEMBER, testUser, true)
    private var testChannel2 = Channel(2L, "testChannel2", ChannelRole.MEMBER, testUser2, true)

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
            testUser = userRepository.save(testUser)
            testUser2 = userRepository.save(testUser2)
            testUser3 = userRepository.save(testUser3)
            testChannel =
                testChannel.copy(
                    owner = testUser,
                    membersLazy =
                        lazy {
                            mapOf(
                                testUser to ChannelRole.OWNER,
                                testUser2 to ChannelRole.MEMBER,
                                testUser3 to ChannelRole.GUEST,
                            )
                        },
                )
            testChannel = channelRepository.save(testChannel)
            testChannel2 = testChannel2.copy(owner = testUser, membersLazy = lazy { mapOf(testUser to ChannelRole.OWNER) })
            testChannel2 = channelRepository.save(testChannel2)
        }
    }

    @Test
    fun `test create message should return message`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val msg = message.value
        assertEquals("test message", msg.content)
        assertEquals(testChannel.id, msg.channelId)
        assertEquals(testUser.id, msg.user.id)
    }

    @Test
    fun `create message channel not found should return failure`() {
        val message =
            messageService.createMessage(
                999L.toIdentifier(),
                "test message",
                testUser,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.ChannelNotFound>(error)
    }

    @Test
    fun `create message guest cannot write should return failure`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser3,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.NoWritePermission>(error)
    }

    @Test
    fun `create message user not in channel should return failure`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                User(1L, "testUser2", "Password123", "test@daw.isel.pt"),
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.UserNotInChannel>(error)
    }

    @Test
    fun `test get channel messages channel not found`() {
        val messages = messageService.getChannelMessages(1L.toIdentifier(), PaginationRequest(0, 10), SortRequest("createdAt"), testUser)
        assertIs<Failure<MessageError>>(messages)
        val error = messages.value
        assertIs<MessageError.ChannelNotFound>(error)
    }

    @Test
    fun `test get channel messages user not in channel`() {
        val messages =
            messageService.getChannelMessages(
                testChannel.id,
                PaginationRequest(0, 10),
                SortRequest("createdAt"),
                User(1L, "testUser2", "Password123", "test@daw.isel.pt"),
            )
        assertIs<Failure<MessageError>>(messages)
        val error = messages.value
        assertIs<MessageError.UserNotInChannel>(error)
    }

    @Test
    fun `test get channel messages should return empty`() {
        val messages = messageService.getChannelMessages(testChannel.id, PaginationRequest(0, 10), SortRequest("createdAt"), testUser)
        assertIs<Success<Pagination<Message>>>(messages)
        assertTrue(messages.value.items.isEmpty())
        val pagination = messages.value.info
        assertEquals(0, pagination.total)
        assertNull(pagination.nextPage)
        assertNull(pagination.prevPage)
        assertEquals(1, pagination.currentPage)
        assertEquals(0, pagination.totalPages)
    }

    @Test
    fun `test get channel messages should return messages`() {
        messageService.createMessage(
            testChannel.id,
            "test message 1",
            testUser,
        )
        val messages = messageService.getChannelMessages(testChannel.id, PaginationRequest(0, 10), SortRequest("createdAt"), testUser)
        assertIs<Success<Pagination<Message>>>(messages)
        val result = messages.value
        assertEquals(1, result.items.size)
        val msg = result.items[0]
        assertEquals("test message 1", msg.content)
        assertEquals(testChannel.id, msg.channelId)
        assertEquals(testUser.id, msg.user.id)
        val pagination = result.info
        assertEquals(1, pagination.total)
        assertNull(pagination.nextPage)
        assertNull(pagination.prevPage)
        assertEquals(1, pagination.currentPage)
        assertEquals(1, pagination.totalPages)
    }

    @Test
    fun `get messages paginated should return messages`() {
        messageService.createMessage(
            testChannel.id,
            "test message 1",
            testUser,
        )
        messageService.createMessage(
            testChannel.id,
            "test message 2",
            testUser,
        )
        val messages =
            messageService.getChannelMessages(
                testChannel.id,
                PaginationRequest(0, 1),
                SortRequest("createdAt"),
                testUser,
            )
        assertIs<Success<Pagination<Message>>>(messages)
        val result = messages.value
        assertEquals(1, result.items.size)
        val msg = result.items[0]
        assertEquals("test message 1", msg.content)
        assertEquals(testChannel.id, msg.channelId)
        assertEquals(testUser.id, msg.user.id)
        val pagination = result.info
        assertEquals(2, pagination.total)
        assertEquals(2, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.nextPage)
        assertNull(pagination.prevPage)
    }

    @Test
    fun `get messages invalid sort field`() {
        val messages =
            messageService.getChannelMessages(
                testChannel.id,
                PaginationRequest(0, 1),
                SortRequest("invalid"),
                testUser,
            )
        assertIs<Failure<MessageError>>(messages)
        val error = messages.value
        assertIs<MessageError.InvalidSortField>(error)
    }

    @Test
    fun `get messages paginated desc sort should return messages`() {
        messageService.createMessage(
            testChannel.id,
            "test message 1",
            testUser,
        )
        Thread.sleep(100) // To ensure different timestamps
        messageService.createMessage(
            testChannel.id,
            "test message 2",
            testUser,
        )
        val messages =
            messageService.getChannelMessages(
                testChannel.id,
                PaginationRequest(0, 1),
                SortRequest("createdAt", Sort.DESC),
                testUser,
            )
        assertIs<Success<Pagination<Message>>>(messages)
        val result = messages.value
        assertEquals(1, result.items.size)
        val msg = result.items[0]
        assertEquals("test message 2", msg.content)
        assertEquals(testChannel.id, msg.channelId)
        assertEquals(testUser.id, msg.user.id)
        val pagination = result.info
        assertEquals(2, pagination.total)
        assertEquals(2, pagination.totalPages)
        assertEquals(1, pagination.currentPage)
        assertEquals(2, pagination.nextPage)
        assertNull(pagination.prevPage)
    }

    @Test
    fun `get messages paginated page two sort desc should return message`() {
        messageService.createMessage(
            testChannel.id,
            "test message 1",
            testUser,
        )
        Thread.sleep(100) // To ensure different timestamps
        messageService.createMessage(
            testChannel.id,
            "test message 2",
            testUser,
        )
        val messages =
            messageService.getChannelMessages(
                testChannel.id,
                PaginationRequest(1, 1),
                SortRequest("createdAt", Sort.DESC),
                testUser,
            )
        assertIs<Success<Pagination<Message>>>(messages)
        val result = messages.value
        assertEquals(1, result.items.size)
        val msg = result.items[0]
        assertEquals("test message 1", msg.content)
        assertEquals(testChannel.id, msg.channelId)
        assertEquals(testUser.id, msg.user.id)
        val pagination = result.info
        assertEquals(2, pagination.total)
        assertEquals(2, pagination.totalPages)
        assertEquals(2, pagination.currentPage)
        assertNull(pagination.nextPage)
        assertEquals(1, pagination.prevPage)
    }

    @Test
    fun `update message message not found should return failure`() {
        val message =
            messageService.updateMessage(
                testChannel.id,
                1L.toIdentifier(),
                "test message",
                testUser,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.MessageNotFound>(error)
    }

    @Test
    fun `update message channel not found should return failure`() {
        val message =
            messageService.updateMessage(
                1L.toIdentifier(),
                1L.toIdentifier(),
                "test message",
                testUser,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.ChannelNotFound>(error)
    }

    @Test
    fun `update message cannot edit message should return failure`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val update =
            messageService.updateMessage(
                testChannel.id,
                message.value.id,
                "test message",
                testUser2,
            )
        assertIs<Failure<MessageError>>(update)
        val error = update.value
        assertIs<MessageError.CannotEditMessage>(error)
    }

    @Test
    fun `update message should return updated message`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        assertNull(message.value.editedAt)
        val update =
            messageService.updateMessage(
                testChannel.id,
                message.value.id,
                "updated message",
                testUser,
            )
        assertIs<Success<Message>>(update)
        val updated = update.value
        val updatedMessage = messageService.getMessageById(testChannel.id, message.value.id, testUser)
        assertIs<Success<Message>>(updatedMessage)
        val msg = updatedMessage.value
        assertEquals("updated message", msg.content)
        assertNotNull(msg.editedAt)
        assertEquals(updated.editedAt!!.truncatedTo(ChronoUnit.MILLIS), msg.editedAt!!.truncatedTo(ChronoUnit.MILLIS))
    }

    @Test
    fun `delete message message not found should return failure`() {
        val message =
            messageService.deleteMessage(
                testChannel.id,
                1L.toIdentifier(),
                testUser,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.MessageNotFound>(error)
    }

    @Test
    fun `delete message channel not found should return failure`() {
        val message =
            messageService.deleteMessage(
                1L.toIdentifier(),
                1L.toIdentifier(),
                testUser,
            )
        assertIs<Failure<MessageError>>(message)
        val error = message.value
        assertIs<MessageError.ChannelNotFound>(error)
    }

    @Test
    fun `delete message cannot delete message should return failure`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val delete =
            messageService.deleteMessage(
                testChannel.id,
                message.value.id,
                testUser2,
            )
        assertIs<Failure<MessageError>>(delete)
        val error = delete.value
        assertIs<MessageError.CannotDeleteMessage>(error)
    }

    @Test
    fun `delete message should delete message`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val delete =
            messageService.deleteMessage(
                testChannel.id,
                message.value.id,
                testUser,
            )
        assertIs<Success<Unit>>(delete)
        val deleted = messageService.getMessageById(testChannel.id, message.value.id, testUser)
        assertIs<Failure<MessageError>>(deleted)
        val error = deleted.value
        assertIs<MessageError.MessageNotFound>(error)
    }

    @Test
    fun `delete message channel owner can delete message`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser2,
            )
        assertIs<Success<Message>>(message)
        val delete =
            messageService.deleteMessage(
                testChannel.id,
                message.value.id,
                testUser,
            )
        assertIs<Success<Unit>>(delete)
        val deleted = messageService.getMessageById(testChannel.id, message.value.id, testUser)
        assertIs<Failure<MessageError>>(deleted)
        val error = deleted.value
        assertIs<MessageError.MessageNotFound>(error)
    }

    @Test
    fun `find message by id should return message`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val msg = message.value
        val found = messageService.getMessageById(testChannel.id, msg.id, testUser)
        assertIs<Success<Message>>(found)
        assertEquals(msg, found.value)
    }

    @Test
    fun `find message by id message not found should return failure`() {
        val found = messageService.getMessageById(testChannel.id, 1L.toIdentifier(), testUser)
        assertIs<Failure<MessageError>>(found)
        val error = found.value
        assertIs<MessageError.MessageNotFound>(error)
    }

    @Test
    fun `find message by id channel not found should return failure`() {
        val found = messageService.getMessageById(1L.toIdentifier(), 1L.toIdentifier(), testUser)
        assertIs<Failure<MessageError>>(found)
        val error = found.value
        assertIs<MessageError.ChannelNotFound>(error)
    }

    @Test
    fun `find message by id user not in channel should return failure`() {
        val message =
            messageService.createMessage(
                testChannel2.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val found = messageService.getMessageById(testChannel2.id, message.value.id, testUser2)
        assertIs<Failure<MessageError>>(found)
        val error = found.value
        assertIs<MessageError.UserNotInChannel>(error)
    }

    @Test
    fun `find message by id message in different channel`() {
        val message =
            messageService.createMessage(
                testChannel.id,
                "test message",
                testUser,
            )
        assertIs<Success<Message>>(message)
        val found = messageService.getMessageById(testChannel2.id, message.value.id, testUser)
        assertIs<Failure<MessageError>>(found)
        val error = found.value
        assertIs<MessageError.MessageNotFound>(error)
    }
}
