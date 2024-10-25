package im.domain.user

import im.domain.wrappers.email.Email
import im.domain.wrappers.email.toEmail
import im.domain.wrappers.identifier.Identifier
import im.domain.wrappers.identifier.toIdentifier
import im.domain.wrappers.name.Name
import im.domain.wrappers.name.toName
import im.domain.wrappers.password.Password
import im.domain.wrappers.password.toPassword

/**
 * User domain class.
 *
 * @property id The unique identifier of the user.
 * @property name The name of the user.
 * @property password The password of the user.
 * @property email The email of the user.
 */
data class User(
    val id: Identifier = Identifier(0),
    val name: Name,
    val password: Password,
    val email: Email,
) {
    companion object {
        operator fun invoke(
            id: Long = 0,
            name: String,
            password: String,
            email: String,
        ): User =
            User(
                id.toIdentifier(),
                name.toName(),
                password.toPassword(),
                email.toEmail(),
            )
    }
}
