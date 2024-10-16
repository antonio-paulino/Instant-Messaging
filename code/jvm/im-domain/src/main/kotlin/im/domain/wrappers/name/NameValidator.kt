package im.domain.wrappers.name

import im.domain.Either
import im.domain.failure
import im.domain.success

class NameValidator {
    companion object {
        private const val MAX_NAME_LENGTH = 30
        private const val MIN_NAME_LENGTH = 3
    }

    fun validate(value: String): Either<List<NameValidationError>, Unit> {
        val errors = mutableListOf<NameValidationError>()

        if (value.isBlank()) {
            errors.add(NameValidationError.Blank)
        }

        if (value.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            errors.add(NameValidationError.InvalidLength(MIN_NAME_LENGTH, MAX_NAME_LENGTH))
        }

        if (errors.isNotEmpty()) {
            return failure(errors)
        }

        return success(Unit)
    }
}
