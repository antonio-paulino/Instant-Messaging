package im.repositories

import im.TestApp
import im.channel.Channel
import im.messages.Message
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.pagination.PaginationRequest
import im.repository.pagination.Sort
import im.repository.repositories.transactions.TransactionManager
import im.repository.mem.transactions.MemTransactionManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import im.user.User
import im.wrappers.toIdentifier
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class MessageRepositoryTest(
    @Autowired private val transactionManagerJpa: TransactionManagerJpa
) {
    private lateinit var testChannel: Channel
    private lateinit var testChannel2: Channel
    private lateinit var testUser: User
    private lateinit var testMessage1: Message
    private lateinit var testMessage2: Message
    private lateinit var testMessage3: Message

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
            testUser = userRepository.save(User(1L, "testUser", "password", "user1@daw.isel.pt"))

            testChannel = channelRepository.save(Channel(1L, "General", testUser, true))
            testChannel2 = channelRepository.save(Channel(2L, "Random", testUser, true))

            testMessage1 = Message(
                1L,
                testChannel,
                testUser,
                "Test message 1",
                LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
            )
            testMessage2 = Message(
                2L,
                testChannel,
                testUser,
                "Another message",
                LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MILLIS)
            )
            testMessage3 = Message(
                3L, testChannel2, testUser, "Last message", LocalDateTime.now().truncatedTo(
                    ChronoUnit.MILLIS
                )
            )
        })
    }


    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return null for non-existent id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val foundMessage = messageRepository.findById((999L).toIdentifier())
            assertNull(foundMessage)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return messages by channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            val messages = messageRepository.findByChannel(testChannel)
            assertEquals(2, messages.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return empty list when no messages exist in channel`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val messages = messageRepository.findByChannel(testChannel)
            assertEquals(0, messages.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should return latest messages in channel 1 ordered by createdAt`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage2)
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage3)
            val (latestMessages, pagination) = messageRepository.findByChannel(
                testChannel,
                PaginationRequest(1, 2, Sort.DESC)
            )
            assertEquals(1, pagination.currentPage)
            assertEquals(null, pagination.nextPage)
            assertEquals(null, pagination.prevPage)
            assertEquals(2, pagination.total)
            assertEquals(1, pagination.totalPages)
            assertEquals(2, latestMessages.count())
            assertEquals(testMessage2.content, latestMessages.first().content)
            assertEquals(testMessage1.content, latestMessages.last().content)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `delete channel should delete message`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            assertEquals(1, messageRepository.count())
            channelRepository.delete(testChannel)
            channelRepository.flush()
            messageRepository.flush()
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `delete user should delete messages`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            assertEquals(1, messageRepository.count())
            userRepository.delete(testUser)
            userRepository.flush()
            messageRepository.flush()
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `pagination should return correct messages`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            messageRepository.save(testMessage3)
            val (firstPage, pagination1) = messageRepository.find(PaginationRequest(1, 2))
            val (secondPage, pagination2) = messageRepository.find(PaginationRequest(2, 2))
            assertEquals(1, pagination1.currentPage)
            assertEquals(2, pagination1.nextPage)
            assertEquals(3, pagination1.total)
            assertEquals(2, pagination1.totalPages)
            assertEquals(null, pagination1.prevPage)
            assertEquals(2, pagination2.currentPage)
            assertEquals(null, pagination2.nextPage)
            assertEquals(3, pagination2.total)
            assertEquals(2, pagination2.totalPages)
            assertEquals(1, pagination2.prevPage)
            assertEquals(2, firstPage.size)
            assertEquals(1, secondPage.size)
            assertEquals(testMessage1.content, firstPage.first().content)
            assertEquals(testMessage3.content, secondPage.first().content)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `pagination on empty repository should return empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val (messages) = messageRepository.find(PaginationRequest(1, 2))
            assertTrue(messages.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should save all messages`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val messages = listOf(testMessage1, testMessage2)
            val savedMessages = messageRepository.saveAll(messages)
            assertEquals(2, savedMessages.size)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should update message`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedMessage = messageRepository.save(testMessage1)
            val updatedMessage = savedMessage.copy(content = "Updated message")
            val result = messageRepository.save(updatedMessage)
            assertEquals(updatedMessage.content, result.content)
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete message by id`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedMessage = messageRepository.save(testMessage1)
            messageRepository.deleteById(savedMessage.id)
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete multiple messages by ids`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedMessage1 = messageRepository.save(testMessage1)
            val savedMessage2 = messageRepository.save(testMessage2)
            messageRepository.deleteAllById(listOf(savedMessage1.id, savedMessage2.id))
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete message entity`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedMessage = messageRepository.save(testMessage1)
            messageRepository.delete(savedMessage)
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should delete all messages`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            messageRepository.deleteAll()
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle save of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val result = messageRepository.saveAll(emptyList())
            assertTrue(result.isEmpty())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `should handle delete of empty list`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.deleteAll(emptyList())
            assertEquals(0, messageRepository.count())
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return true for existing message`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            val savedMessage = messageRepository.save(testMessage1)
            assertTrue(messageRepository.existsById(savedMessage.id))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `exists by id should return false for non-existing message`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            assertFalse(messageRepository.existsById((999L).toIdentifier()))
        })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    open fun `count should return correct number of messages`(transactionManager: TransactionManager) {
        setup(transactionManager)
        transactionManager.run({
            messageRepository.save(testMessage1)
            messageRepository.save(testMessage2)
            assertEquals(2, messageRepository.count())
        })
    }
}
