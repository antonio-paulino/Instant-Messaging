package im.domain.wrappers.password

import im.domain.Either
import im.domain.failure
import im.domain.success

class PasswordValidator(
    private val minLength: Int = MIN_LENGTH,
    private val maxLength: Int = MAX_LENGTH,
    private val minLowercase: Int = MIN_LOWERCASE,
    private val minUppercase: Int = MIN_UPPERCASE,
    private val minDigits: Int = MIN_DIGITS,
) {
    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 80

        private const val MIN_LOWERCASE = 1
        private const val MIN_UPPERCASE = 1
        private const val MIN_DIGITS = 1
    }

    fun validate(value: String): Either<List<PasswordValidationError>, Unit> {
        val errors = mutableListOf<PasswordValidationError>()

        if (value.isBlank()) {
            errors.add(PasswordValidationError.Blank)
        }

        if (value.any { it.isWhitespace() }) {
            errors.add(PasswordValidationError.CannotContainWhitespace)
        }

        if (value.length !in minLength..maxLength) {
            errors.add(PasswordValidationError.InvalidLength(minLength, maxLength))
        }

        if (value.count { it.isLowerCase() } < minLowercase) {
            errors.add(PasswordValidationError.NotEnoughLowercaseLetters(minLowercase))
        }

        if (value.count { it.isUpperCase() } < minUppercase) {
            errors.add(PasswordValidationError.NotEnoughUppercaseLetters(minUppercase))
        }

        if (value.count { it.isDigit() } < minDigits) {
            errors.add(PasswordValidationError.NotEnoughDigits(minDigits))
        }

        if (errors.isNotEmpty()) {
            return failure(errors)
        }

        return success(Unit)
    }
}
