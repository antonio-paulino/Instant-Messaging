package im.domain.wrappers.identifier

import im.domain.Either
import im.domain.failure
import im.domain.success

class IdentifierValidator {
    fun validate(value: Long): Either<List<IdentifierValidationError>, Unit> {
        val errors = mutableListOf<IdentifierValidationError>()

        if (value < 0) {
            errors.add(IdentifierValidationError.NegativeValue)
        }

        if (errors.isNotEmpty()) {
            return failure(errors)
        }

        return success(Unit)
    }
}
