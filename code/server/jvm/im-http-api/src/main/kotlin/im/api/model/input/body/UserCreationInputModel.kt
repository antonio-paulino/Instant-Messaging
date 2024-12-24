package im.api.model.input.body

import im.api.model.input.wrappers.Email
import im.api.model.input.wrappers.Name
import im.api.model.input.wrappers.Password
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID

/**
 * Input model for creating a user.
 *
 * @property username The username of the user.
 * @property password The password of the user.
 * @property email The email of the user.
 * @property invitation The invitation code for the user.
 */
data class UserCreationInputModel(
    @field:Valid
    val username: Name,
    @field:Valid
    val password: Password,
    @field:Valid
    val email: Email,
    @UUID(message = "Invalid invitation code format")
    @field:NotNull(message = "Invitation code is required")
    val invitation: String,
) {
    constructor(
        username: String,
        password: String,
        email: String,
        invitation: String,
    ) : this(
        Name(username),
        Password(password),
        Email(email),
        invitation,
    )
}
