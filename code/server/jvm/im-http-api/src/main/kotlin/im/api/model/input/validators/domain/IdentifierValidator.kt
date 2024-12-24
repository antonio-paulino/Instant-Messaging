package im.api.model.input.validators.domain

import im.domain.Failure
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid identifier.
 */
@Constraint(validatedBy = [IdentifierValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class IdentifierValid(
    val message: String = "Invalid identifier",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Identifier validator.
 *
 * @see im.domain.wrappers.identifier.IdentifierValidator
 */
class IdentifierValidator : ConstraintValidator<IdentifierValid, String> {
    private val validator =
        im.domain.wrappers.identifier
            .IdentifierValidator()

    override fun isValid(
        p0: String,
        p1: ConstraintValidatorContext?,
    ): Boolean {
        p1?.disableDefaultConstraintViolation()

        val num = p0.toLongOrNull()

        if (num == null) {
            p1?.buildConstraintViolationWithTemplate("Identifier must be a number")?.addConstraintViolation()
            return false
        }

        val validation = validator.validate(num)

        if (validation is Failure) {
            validation.value.forEach {
                p1?.buildConstraintViolationWithTemplate(it.toErrorMessage())?.addConstraintViolation()
            }
            return false
        }

        return true
    }
}
