package transactions

import org.hibernate.type.SerializationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import user.User
import user.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@SpringBootTest
@ContextConfiguration(classes = [TestAppTransactions::class])
class TransactionManagerTest(
    @Autowired private val transactionManager: TransactionManagerJpa,
    @Autowired private val userRepository: UserRepository
) {

    private val testUser = User(1L, "testUser", "password", "user1@daw.isel.pt")

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `test transaction run`() {
        val ran = transactionManager.run({
            true
        })
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
        assertTrue(user.isEmpty)
    }

    @Test
    fun `test transaction run with serialization exception retries and saves`() {
        var retried = false
        var testUser = User(1L, "testUser", "password","user1@daw.isel.pt")
        assertDoesNotThrow {
            transactionManager.run({
                testUser = userRepository.save(testUser)
                if (!retried) {
                    retried = true
                    throw SerializationException("test", null)
                }
            }, TransactionIsolation.SERIALIZABLE)
        }
        val user = userRepository.findById(testUser.id)
        assertTrue(user.isPresent)
    }

    @Test
    fun `test transaction run with serialization exception fails after 3 tries`() {
        var retries = 0
        assertThrows<SerializationException> {
            transactionManager.run({
                retries++
                throw SerializationException("test", null)
            }, TransactionIsolation.SERIALIZABLE)
        }
        assertEquals(3, retries)
    }

}