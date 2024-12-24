package im.api.model.input.validators.domain

import im.domain.channel.ChannelRole
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid [ChannelRole].
 */
@Constraint(validatedBy = [RoleValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Role(
    val message: String = "Invalid role",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Role validator.
 *
 * @see ChannelRole
 */
class RoleValidator : ConstraintValidator<Role, String> {
    override fun isValid(
        value: String,
        context: ConstraintValidatorContext?,
    ): Boolean {
        context?.disableDefaultConstraintViolation()

        if (!ChannelRole.entries.any { it.name == value.uppercase() }) {
            context
                ?.buildConstraintViolationWithTemplate(
                    "Role must be one of ${ChannelRole.entries.joinToString { it.name }}",
                )?.addConstraintViolation()
            return false
        }

        return true
    }
}
