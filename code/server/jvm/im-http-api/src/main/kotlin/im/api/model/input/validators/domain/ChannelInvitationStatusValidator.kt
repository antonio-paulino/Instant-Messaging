package im.api.model.input.validators.domain

import im.domain.invitations.ChannelInvitationStatus
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validates if a field is a valid channel invitation status.
 */
@Constraint(validatedBy = [ChannelInvitationStatusValidator::class])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelInvitationStatusValid(
    val message: String = "Invalid status",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Channel invitation status validator.
 *
 * @see ChannelInvitationStatus
 */
class ChannelInvitationStatusValidator : ConstraintValidator<ChannelInvitationStatusValid, String> {
    override fun isValid(
        p0: String,
        p1: ConstraintValidatorContext?,
    ): Boolean {
        p1?.disableDefaultConstraintViolation()

        if (!ChannelInvitationStatus.entries.any { it.name == p0.uppercase() }) {
            p1
                ?.buildConstraintViolationWithTemplate(
                    "Status must be one of ${ChannelInvitationStatus.entries.joinToString { it.name }}",
                )?.addConstraintViolation()
            return false
        }

        return true
    }
}
