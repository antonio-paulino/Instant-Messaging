package im.services.invitations

import im.domain.channel.ChannelRole
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.user.User
import im.domain.wrappers.Identifier
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import im.services.Either
import im.services.Failure
import im.services.failure
import im.services.success
import jakarta.inject.Named
import java.time.LocalDateTime

@Named
class InvitationServiceImpl(
    val transactionManager: TransactionManager,
) : InvitationService {
    companion object {
        private const val INVITATION_MAX_EXPIRATION_DAYS = 30L
        private const val INVITATION_MIN_EXPIRATION_MINUTES = 15L
        private const val DEFAULT_SORT = "id"
        private val validSortFields = setOf("id", "role", "expiresAt")
    }

    override fun createInvitation(
        channelId: Identifier,
        inviteeId: Identifier,
        expirationDate: LocalDateTime,
        role: ChannelRole,
        inviter: User,
    ): Either<InvitationError, ChannelInvitation> =
        transactionManager.run {
            val expiryValidation = validateExpirationDate(expirationDate)

            if (expiryValidation is Failure) {
                return@run expiryValidation
            }

            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(InvitationError.ChannelNotFound)

            if (channel.owner != inviter) {
                return@run Failure(InvitationError.UserCannotInviteToChannel)
            }

            val invitee =
                userRepository.findById(inviteeId)
                    ?: return@run Failure(InvitationError.InviteeNotFound)

            val invitation = channelInvitationRepository.findByInviteeAndChannel(invitee, channel)

            if (invitation != null && invitation.isValid) {
                return@run Failure(InvitationError.InvitationAlreadyExists)
            }

            val member = channelRepository.getMember(channel, invitee)

            if (member != null) {
                return@run Failure(InvitationError.InviteeAlreadyMember)
            }

            val newInvitation =
                channelInvitationRepository.save(
                    ChannelInvitation(
                        channel = channel,
                        inviter = inviter,
                        invitee = invitee,
                        role = role,
                        expiresAt = expirationDate,
                    ),
                )

            success(newInvitation)
        }

    override fun getInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        user: User,
    ): Either<InvitationError, ChannelInvitation> =
        transactionManager.run {
            channelRepository.findById(channelId)
                ?: return@run Failure(InvitationError.ChannelNotFound)

            val invitation =
                channelInvitationRepository.findById(inviteId)
                    ?: return@run Failure(InvitationError.InvitationNotFound)

            if (invitation.inviter != user && invitation.invitee != user) {
                return@run Failure(InvitationError.UserCannotAccessInvitation)
            }

            success(invitation)
        }

    override fun getChannelInvitations(
        channelId: Identifier,
        user: User,
        sortRequest: SortRequest,
    ): Either<InvitationError, List<ChannelInvitation>> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(InvitationError.ChannelNotFound)

            if (channel.owner != user) {
                return@run Failure(InvitationError.UserCannotAccessInvitation)
            }

            val sort = sortRequest.sortBy ?: DEFAULT_SORT

            if (sort !in validSortFields) {
                return@run Failure(InvitationError.InvalidSortField(sort, validSortFields.toList()))
            }

            val invitations =
                channelInvitationRepository.findByChannel(
                    channel,
                    ChannelInvitationStatus.PENDING,
                    sortRequest.copy(sortBy = sort),
                )

            val validInvitations = invitations.filter { it.isValid }

            success(validInvitations)
        }

    override fun updateInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        role: ChannelRole,
        expirationDate: LocalDateTime,
        user: User,
    ): Either<InvitationError, Unit> =
        transactionManager.run {
            val expiryValidation = validateExpirationDate(expirationDate)

            if (expiryValidation is Failure) {
                return@run expiryValidation
            }

            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(InvitationError.ChannelNotFound)

            if (channel.owner != user) {
                return@run Failure(InvitationError.UserCannotUpdateInvitation)
            }

            val invitation =
                channelInvitationRepository.findById(inviteId)
                    ?: return@run Failure(InvitationError.InvitationNotFound)

            if (channel != invitation.channel) {
                return@run Failure(InvitationError.InvitationNotFound)
            }

            if (!invitation.isValid) {
                return@run Failure(InvitationError.InvitationInvalid)
            }

            channelInvitationRepository.save(invitation.copy(expiresAt = expirationDate, role = role))
            success(Unit)
        }

    override fun deleteInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        user: User,
    ): Either<InvitationError, Unit> =
        transactionManager.run {
            val channel =
                channelRepository.findById(channelId)
                    ?: return@run Failure(InvitationError.ChannelNotFound)

            val invitation =
                channelInvitationRepository.findById(inviteId)
                    ?: return@run Failure(InvitationError.InvitationNotFound)

            if (channel != invitation.channel) {
                return@run Failure(InvitationError.InvitationNotFound)
            }

            if (invitation.inviter != user) {
                return@run Failure(InvitationError.UserCannotDeleteInvitation)
            }

            channelInvitationRepository.delete(invitation)

            success(Unit)
        }

    override fun getUserInvitations(
        userId: Identifier,
        user: User,
        sortRequest: SortRequest,
    ): Either<InvitationError, List<ChannelInvitation>> =
        transactionManager.run {
            if (userId != user.id) {
                return@run Failure(InvitationError.UserCannotAccessInvitation)
            }

            val sort = sortRequest.sortBy ?: DEFAULT_SORT

            if (!validSortFields.contains(sort)) {
                return@run Failure(InvitationError.InvalidSortField(sort, validSortFields.toList()))
            }

            val invitations = channelInvitationRepository.findByInvitee(user, sortRequest.copy(sortBy = sort))

            success(invitations)
        }

    override fun acceptOrRejectInvitation(
        userId: Identifier,
        invitationIdentifier: Identifier,
        status: ChannelInvitationStatus,
        user: User,
    ): Either<InvitationError, Unit> =
        transactionManager.run {
            if (userId != user.id) {
                return@run Failure(InvitationError.UserCannotAccessInvitation)
            }

            val invitation =
                channelInvitationRepository.findById(invitationIdentifier)
                    ?: return@run Failure(InvitationError.InvitationNotFound)

            if (invitation.invitee != user) {
                return@run Failure(InvitationError.InvitationNotFound)
            }

            if (!invitation.isValid) {
                return@run Failure(InvitationError.InvitationInvalid)
            }

            if (status == ChannelInvitationStatus.ACCEPTED) {
                val channel = invitation.channel
                val role = invitation.role
                channelInvitationRepository.save(invitation.accept())
                channelRepository.save(channel.addMember(user, role))
            } else {
                channelInvitationRepository.save(invitation.reject())
            }

            success(Unit)
        }

    private fun validateExpirationDate(expirationDate: LocalDateTime): Either<InvitationError, Unit> {
        if (expirationDate.isBefore(LocalDateTime.now().plusMinutes(INVITATION_MIN_EXPIRATION_MINUTES))) {
            return failure(
                InvitationError.InvalidInvitationExpiration(
                    "Minimum expiration time is $INVITATION_MIN_EXPIRATION_MINUTES minutes",
                ),
            )
        }
        if (expirationDate.isAfter(LocalDateTime.now().plusDays(INVITATION_MAX_EXPIRATION_DAYS))) {
            return failure(
                InvitationError.InvalidInvitationExpiration(
                    "Maximum expiration time is $INVITATION_MAX_EXPIRATION_DAYS days",
                ),
            )
        }
        return success(Unit)
    }
}
