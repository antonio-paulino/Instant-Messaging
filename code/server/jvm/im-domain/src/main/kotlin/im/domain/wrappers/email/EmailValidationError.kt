package im.domain.wrappers.email

sealed class EmailValidationError(
    private val defaultMessage: String = "Email is invalid",
) {
    data object Blank : EmailValidationError("Email cannot be blank")

    data object InvalidFormat : EmailValidationError("Email has an invalid format")

    data class InvalidLength(
        val min: Int,
        val max: Int,
    ) : EmailValidationError("Email must be between $min and $max characters")

    fun toErrorMessage(): String = defaultMessage
}

fun List<EmailValidationError>.toErrorMessage(): String = joinToString("\n") { it.toErrorMessage() }
