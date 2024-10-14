package im.transactions

import im.domain.channel.Channel
import im.domain.channel.ChannelRole
import im.domain.user.User
import im.repository.jpa.repositories.ChannelRepositoryImpl
import im.repository.jpa.repositories.UserRepositoryImpl
import im.repository.jpa.transactions.TransactionManagerJpa
import im.repository.repositories.transactions.TransactionIsolation
import org.hibernate.Hibernate
import org.hibernate.type.SerializationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class TransactionManagerTest {
    private var testUser = User(1L, "testUser", "password", "user1@daw.isel.pt")
    private var testChannel = Channel(1L, "testChannel", testUser, true)

    @Autowired
    private lateinit var transactionManager: TransactionManagerJpa

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @Autowired
    private lateinit var channelRepository: ChannelRepositoryImpl

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        channelRepository.deleteAll()

        testUser = User(1L, "testUser", "password", "user1@daw.isel.pt")
        testChannel = Channel(1L, "testChannel", testUser, true)
    }

    @Test
    fun `test transaction run`() {
        val ran =
            transactionManager.run {
                true
            }
        assertTrue(ran)
    }

    @Test
    fun `test transaction run with exception aborts and throws exception`() {
        assertFailsWith<Exception> {
            transactionManager.run {
                userRepository.save(testUser)
                throw Exception()
            }
        }
        val user = userRepository.findById(testUser.id)
        assertNull(user)
    }

    @Test
    fun `test transaction run with serialization exception retries and saves`() {
        var retried = false
        var testUser = User(1L, "testUser", "password", "user1@daw.isel.pt")
        assertDoesNotThrow {
            transactionManager.run(TransactionIsolation.SERIALIZABLE) {
                testUser = userRepository.save(testUser)
                if (!retried) {
                    retried = true
                    throw SerializationException("test", null)
                }
            }
        }
        val user = userRepository.findById(testUser.id)
        assertEquals(testUser, user)
    }

    @Test
    fun `test transaction run with serialization exception fails after 3 tries`() {
        var retries = 0
        assertThrows<SerializationException> {
            transactionManager.run(TransactionIsolation.SERIALIZABLE) {
                retries++
                throw SerializationException("test", null)
            }
        }
        assertEquals(3, retries)
    }

    @Test
    fun `test transaction lazy value initialization`() {
        testUser = userRepository.save(testUser)
        testChannel = testChannel.copy(owner = testUser, membersLazy = lazy { mapOf(testUser to ChannelRole.OWNER) })
        testChannel = channelRepository.save(testChannel)
        transactionManager.run {
            testChannel = channelRepository.findById(testChannel.id)!!
            testChannel.members
        }
        assertTrue(Hibernate.isInitialized(testChannel.members))
    }

    @Test
    fun `test lazy value initialization outside of transaction throws exception`() {
        testUser = userRepository.save(testUser)
        testChannel = testChannel.copy(owner = testUser, membersLazy = lazy { mapOf(testUser to ChannelRole.OWNER) })
        testChannel = channelRepository.save(testChannel)
        transactionManager.run {
            testChannel = channelRepository.findById(testChannel.id)!!
        }
        assertThrows<Exception> {
            testChannel.members
        }
    }

    @Test
    fun `test lazy value initialization other transaction`() {
        testUser = userRepository.save(testUser)
        testChannel = testChannel.copy(owner = testUser, membersLazy = lazy { mapOf(testUser to ChannelRole.OWNER) })
        testChannel = channelRepository.save(testChannel)
        transactionManager.run {
            testChannel = channelRepository.findById(testChannel.id)!!
            testChannel.members
        }
        transactionManager.run {
            val members = testChannel.members
            assertTrue(Hibernate.isInitialized(members))
        }
    }
}
