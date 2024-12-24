package im.api.model.input.validators.domain

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid name.
 */
@Constraint(validatedBy = [NameValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class NameValid(
    val message: String = "Invalid name",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Name validator
 *
 * @see im.domain.wrappers.name.NameValidator
 */
class NameValidator : ConstraintValidator<NameValid, String> {
    private val validator =
        im.domain.wrappers.name
            .NameValidator()

    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        val validation = validator.validate(value)

        if (validation is im.domain.Failure) {
            validation.value.forEach {
                context?.buildConstraintViolationWithTemplate(it.toErrorMessage())?.addConstraintViolation()
            }
            return false
        }

        return true
    }
}
