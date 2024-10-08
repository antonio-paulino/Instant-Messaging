package im.user

import im.wrappers.Email
import im.wrappers.Identifier
import im.wrappers.Name
import im.wrappers.Password
import im.wrappers.toEmail
import im.wrappers.toIdentifier
import im.wrappers.toName
import im.wrappers.toPassword

data class User(
    val id: Identifier = Identifier(0),
    val name: Name,
    val password: Password,
    val email: Email
) {
    companion object {
        operator fun invoke(id: Long = 0, name: String, password: String, email: String): User {
            return User(
                id = id.toIdentifier(),
                name = name.toName(),
                password = password.toPassword(),
                email = email.toEmail()
            )
        }
    }
}