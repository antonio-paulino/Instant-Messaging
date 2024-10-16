package im.domain.wrappers.email

import im.domain.Either
import im.domain.failure
import im.domain.success

class EmailValidator {
    companion object {
        private const val MAX_EMAIL_LENGTH = 50
        private const val MIN_EMAIL_LENGTH = 8
        private const val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$"
    }

    fun validate(email: String): Either<List<EmailValidationError>, Unit> {
        val errors = mutableListOf<EmailValidationError>()

        if (email.isBlank()) {
            errors.add(EmailValidationError.Blank)
        }

        if (!email.matches(Regex(EMAIL_REGEX))) {
            errors.add(EmailValidationError.InvalidFormat)
        }

        if (email.length !in MIN_EMAIL_LENGTH..MAX_EMAIL_LENGTH) {
            errors.add(EmailValidationError.InvalidLength(MIN_EMAIL_LENGTH, MAX_EMAIL_LENGTH))
        }

        if (errors.isNotEmpty()) {
            return failure(errors)
        }

        return success(Unit)
    }
}
