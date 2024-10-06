package im.model.input.body

import im.model.input.validators.OneNotNull
import jakarta.validation.constraints.NotNull

@OneNotNull(
    fields = ["username", "email"],
    message = "Either username or email is required"
)
data class AuthenticationInputModel(
    val username: String?,

    @NotNull(message = "Password is required")
    val password: String,

    val email: String?
)
