package im.api.model.input.validators

import im.domain.channel.ChannelRole
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

class RoleValidator : ConstraintValidator<Role, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return value != null && ChannelRole.entries.any { it.name == value.uppercase() && it != ChannelRole.OWNER }
    }
}

@Constraint(validatedBy = [RoleValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Role(
    val message: String = "Role must be one of: MEMBER, GUEST",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
