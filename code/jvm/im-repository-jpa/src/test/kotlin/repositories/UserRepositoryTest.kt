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
import kotlin.test.assertNotNull

@SpringBootTest
@ContextConfiguration(classes = [TestApp::class])
open class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepositoryImpl

    @BeforeEach
    open fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    @Transactional
    open fun `should save and find user by name`() {
        val newUser = User(
            id = 1,
            name = "test",
            password = "test",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        userRepository.save(newUser)
        val user = userRepository.findByName("test")
        assertNotNull(user)
        assertEquals(newUser.name, user.name)
    }

    @Test
    @Transactional
    open fun `should find user by id`() {
        val newUser = User(
            name = "test2",
            password = "test2",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val savedUser = userRepository.save(newUser)
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
        val newUser = User(
            name = "test3",
            password = "password1",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val savedUser = userRepository.save(newUser)

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
        val newUser = User(
            name = "test4",
            password = "password4",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val savedUser = userRepository.save(newUser)

        assertEquals(newUser.name, savedUser.name)

        userRepository.deleteById(savedUser.id)

        val user = userRepository.findById(savedUser.id)
        assertTrue(user.isEmpty)
    }

    @Test
    @Transactional
    open fun `should find all users`() {
        val user1 = User(
            name = "user1",
            password = "password1",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val user2 = User(
            name = "user2",
            password = "password2",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        userRepository.save(user1)
        userRepository.save(user2)

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
        val user1 = User(
            name = "duplicateUser",
            password = "password1",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        userRepository.save(user1)

        val user2 = User(
            name = "duplicateUser",
            password = "password2",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )

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
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            ownedChannels = emptyList(),
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
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        val user2 = User(
            name = "jane.doe",
            password = "password2",
            ownedChannels = emptyList(),
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
        val newUser = User(
            name = "test5",
            password = "password5",
            ownedChannels = emptyList(),
            joinedChannels = emptyList()
        )
        userRepository.save(newUser)
        val user = userRepository.findByNameAndPassword("test5", "password5")
        assertNotNull(user)
        assertEquals(newUser.name, user.name)
        assertEquals(newUser.password, user.password)
    }
}


