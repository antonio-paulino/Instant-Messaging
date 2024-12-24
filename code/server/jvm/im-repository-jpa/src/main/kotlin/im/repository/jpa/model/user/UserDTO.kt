package im.repository.jpa.model.user

import im.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.stereotype.Component

/**
 * Represents a user in the database.
 *
 * - A user can create multiple channels (one-to-many relationship).
 * - A user can be a member of multiple channels (many-to-many relationship).
 * - A user can send multiple messages (one-to-many relationship).
 * - A user can have multiple sessions (one-to-many relationship).
 * - A user can create many Application invitations (one-to-many relationship).
 * - A user can create many Channel invitations (one-to-many relationship).
 * - A user can receive many Channel invitations (one-to-many relationship)
 *
 * @property id The unique identifier of the user.
 * @property name The name of the user.
 * @property password The password of the user.
 * @property email The email of the user.
 */
@Entity
@Table(name = "users")
@Component
open class UserDTO(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
    @Column(unique = true, nullable = false, length = 30)
    open val name: String = "",
    @Column(nullable = false, length = 100)
    open val password: String = "",
    @Column(unique = true, nullable = false, length = 100)
    open val email: String = "",
) {
    companion object {
        fun fromDomain(user: User): UserDTO =
            UserDTO(
                id = user.id.value,
                name = user.name.value,
                password = user.password.value,
                email = user.email.value,
            )
    }

    fun toDomain(): User =
        User(
            id = id,
            name = name,
            password = password,
            email = email,
        )
}
