package im.model.input.body

import im.model.input.validators.OneNotNull
import im.model.input.wrappers.Email
import im.model.input.wrappers.Name
import im.model.input.wrappers.Password
import jakarta.validation.Valid

@OneNotNull(
    fields = ["username", "email"],
    message = "Either username or email is required"
)
data class AuthenticationInputModel(
    val username: Name?,

    @Valid
    val password: Password,

    val email: Email?
)
