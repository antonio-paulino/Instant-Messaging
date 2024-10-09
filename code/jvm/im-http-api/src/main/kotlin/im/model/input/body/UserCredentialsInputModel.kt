package im.model.input.body

import im.model.input.wrappers.Email
import im.model.input.wrappers.Name
import im.model.input.wrappers.Password
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.UUID

data class UserCredentialsInputModel(
    @Valid
    val username: Name,

    @Valid
    val password: Password,

    @Valid
    val email: Email,

    @UUID(message = "Invalid invitation code format")
    @field:NotNull(message = "Invitation code is required")
    val invitation: String
)