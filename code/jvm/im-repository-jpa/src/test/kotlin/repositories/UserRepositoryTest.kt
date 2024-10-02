package repositories

import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ContextConfiguration
import user.User
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    private var testUser = User(1, "user", "password")
    private var testUser2 = User(2, "user2", "password2")

    @BeforeEach
    open fun setUp() {
        userRepository.deleteAll()
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
    open fun `should find user by id`() {
        val savedUser = userRepository.save(testUser)
        val user = userRepository.findById(savedUser.id)
        assertNotNull(user)
        assertEquals(savedUser.id, user.get().id)
    }

    @Test
    @Transactional
    open fun `should return empty when user not found by name`() {
        val user = userRepository.findByPartialName("non-existing")
        assertTrue(user.isEmpty())
    }

    @Test
    @Transactional
    open fun `should return empty when user not found by id`() {
        val user = userRepository.findById(9999L)
        assertTrue(user.isEmpty)
    }

    @Test
    @Transactional
    open fun `should update user information`() {
        val savedUser = userRepository.save(testUser)
        val updatedUser = savedUser.copy(name = "updatedName", password = "updatedPassword")
        userRepository.save(updatedUser)

        val user = userRepository.findById(savedUser.id)
        assertTrue(user.isPresent)
        assertEquals("updatedName", user.get().name)
        assertEquals("updatedPassword", user.get().password)
    }

    @Test
    @Transactional
    open fun `should delete user by id`() {
        val savedUser = userRepository.save(testUser)

        assertEquals(testUser.name, savedUser.name)

        userRepository.deleteById(savedUser.id)

        val user = userRepository.findById(savedUser.id)
        assertTrue(user.isEmpty)
    }

    @Test
    @Transactional
    open fun `should delete user by entity`() {
        val savedUser = userRepository.save(testUser)

        assertEquals(testUser.name, savedUser.name)

        userRepository.delete(savedUser)

        val user = userRepository.findById(savedUser.id)
        assertTrue(user.isEmpty)
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
            joinedChannels = emptyList()
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            joinedChannels = emptyList()
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
            joinedChannels = emptyList()
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            joinedChannels = emptyList()
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


