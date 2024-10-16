package im.domain.wrappers.password

import im.domain.Failure
import im.domain.Success

/**
 * Password wrapper class that enforces password validation rules.
 *
 * A password must not be blank and must be between 8 and 80 characters.
 */
data class Password(
    val value: String,
) {
    companion object {
        private val validator = PasswordValidator()
    }

    init {
        val validation = validator.validate(value)
        require(validation is Success) { (validation as Failure).value.toErrorMessage() }
    }

    override fun toString(): String = value
}

fun String.toPassword(): Password = Password(this)
