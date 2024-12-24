package im.domain.wrappers.password

sealed class PasswordValidationError(
    private val defaultMessage: String = "Password is invalid",
) {
    data object Blank : PasswordValidationError("Password cannot be blank")

    data class InvalidLength(
        val min: Int,
        val max: Int,
    ) : PasswordValidationError("Password must be between $min and $max characters")

    data class NotEnoughUppercaseLetters(
        val min: Int,
    ) : PasswordValidationError("Password must contain at least $min uppercase letter(s)")

    data class NotEnoughLowercaseLetters(
        val min: Int,
    ) : PasswordValidationError("Password must contain at least $min lowercase letter(s)")

    data class NotEnoughDigits(
        val min: Int,
    ) : PasswordValidationError("Password must contain at least $min digit(s)")

    data object CannotContainWhitespace : PasswordValidationError("Password cannot contain whitespace")

    fun toErrorMessage(): String = defaultMessage
}

fun List<PasswordValidationError>.toErrorMessage(): String = joinToString("\n") { it.toErrorMessage() }
