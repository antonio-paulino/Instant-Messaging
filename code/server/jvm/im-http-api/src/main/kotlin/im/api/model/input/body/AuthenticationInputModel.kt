package im.api.model.input.body

import im.api.model.input.validators.OneNotNull
import im.api.model.input.wrappers.Email
import im.api.model.input.wrappers.Name
import im.api.model.input.wrappers.Password
import jakarta.validation.Valid

/**
 * Input model for login.
 *
 * A user can log in using either their username or email.
 *
 * @property username The username of the user.
 * @property password The password of the user.
 * @property email The email of the user.
 */
@OneNotNull(
    fields = ["username", "email"],
    message = "Either username or email is required",
)
data class AuthenticationInputModel(
    @field:Valid
    val username: Name?,
    @field:Valid
    val password: Password,
    @field:Valid
    val email: Email?,
) {
    constructor(
        username: String?,
        password: String,
        email: String?,
    ) : this(
        if (username != null) Name(username) else null,
        Password(password),
        if (email != null) Email(email) else null,
    )
}
