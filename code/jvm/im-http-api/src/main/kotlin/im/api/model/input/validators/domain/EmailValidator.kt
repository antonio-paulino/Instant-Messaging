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
@Constraint(validatedBy = [EmailValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmailValid(
    val message: String = "Invalid email",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Email validator.
 *
 * @see im.domain.wrappers.email.EmailValidator
 */
class EmailValidator : ConstraintValidator<EmailValid, String> {
    private val validator =
        im.domain.wrappers.email
            .EmailValidator()

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        val validation = validator.validate(value ?: "")

        if (validation is Failure) {
            validation.value.forEach {
                context?.buildConstraintViolationWithTemplate(it.toErrorMessage())?.addConstraintViolation()
            }
            return false
        }

        return true
    }
}
