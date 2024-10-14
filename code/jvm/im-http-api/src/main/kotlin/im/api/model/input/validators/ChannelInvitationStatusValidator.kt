package im.api.model.input.validators

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [ChannelInvitationStatusValidator::class])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChannelInvitationStatus(
    val fields: Array<String> = [],
    val message: String = "New status must be one of ACCEPTED or REJECTED",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ChannelInvitationStatusValidator : ConstraintValidator<ChannelInvitationStatus, String> {
    override fun isValid(
        p0: String,
        p1: ConstraintValidatorContext?,
    ): Boolean {
        return im.domain.invitations.ChannelInvitationStatus.entries.any { it.name == p0.uppercase() } &&
            p0 != im.domain.invitations.ChannelInvitationStatus.PENDING.name
    }
}
