package im.api.model.input.validators.domain

import im.domain.Failure
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid password.
 */
@Constraint(validatedBy = [PasswordValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidPassword(
    val message: String = "Invalid password",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Password validator.
 *
 * @see im.domain.wrappers.password.PasswordValidator
 */
class PasswordValidator : ConstraintValidator<ValidPassword, String> {
    private val validator =
        im.domain.wrappers.password
            .PasswordValidator()

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        val validation = validator.validate(value ?: "")

        if (validation is Failure) {
            validation.value.forEach {
                context.addConstraintViolation(it.toErrorMessage())
            }
            return false
        }

        return true
    }

    private fun ConstraintValidatorContext?.addConstraintViolation(message: String) =
        this?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
}
