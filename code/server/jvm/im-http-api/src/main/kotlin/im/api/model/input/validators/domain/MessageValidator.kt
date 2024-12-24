package im.api.model.input.validators.domain

import im.domain.Failure
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid message.
 */
@Constraint(validatedBy = [MessageValidator::class])
annotation class MessageValid(
    val message: String = "Invalid message",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Message validator.
 *
 * @see im.domain.messages.MessageValidator
 */
class MessageValidator : ConstraintValidator<MessageValid, String> {
    private val validator = im.domain.messages.MessageValidator()

    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        val validation = validator.validate(value)

        if (validation is Failure) {
            validation.value.forEach {
                context?.buildConstraintViolationWithTemplate(it.toErrorMessage())?.addConstraintViolation()
            }
            return false
        }

        return true
    }
}
