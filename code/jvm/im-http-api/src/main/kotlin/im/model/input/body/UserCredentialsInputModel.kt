package im.model.input.body

import im.model.input.validators.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.UUID

data class UserCredentialsInputModel(
    @field:Size(min = 3, max = 30, message = "Name must be between 3 and 50 characters")
    @field:NotBlank(message = "Name must not be blank")
    @field:NotNull(message = "Name is required")
    val username: String,

    @field:Password(message = "Password must be valid")
    @field:NotNull(message = "Password is required")
    val password: String,

    @field:Email(message = "Email must be a valid email address")
    @field:Size(min = 3, max = 50, message = "Email must be between 3 and 50 characters")
    @field:NotNull(message = "Email is required")
    val email: String,

    @UUID(message = "Invalid invitation code format")
    @field:NotNull(message = "Invitation code is required")
    val invitation: String
)