package im.services.invitations

import im.domain.Either
import im.domain.Failure
import im.domain.channel.ChannelRole
import im.domain.failure
import im.domain.invitations.ChannelInvitation
import im.domain.invitations.ChannelInvitationStatus
import im.domain.invitations.ImInvitation
import im.domain.success
import im.domain.user.User
import im.domain.wrappers.identifier.Identifier
import im.repository.pagination.Pagination
import im.repository.pagination.PaginationRequest
import im.repository.pagination.SortRequest
import im.repository.repositories.transactions.TransactionManager
import jakarta.inject.Named
import java.time.LocalDateTime

@Named
class InvitationServiceImpl(
    private val transactionManager: TransactionManager,
    private val config: InvitationConfig,
) : InvitationService {
    companion object {
        private const val DEFAULT_SORT = "id"
        private val validSortFields = setOf("id", "role", "expiresAt")
    }

    override fun createChannelInvitation(
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

            if (role == ChannelRole.OWNER) {
                return@run Failure(InvitationError.OwnerInvitationNotAllowed)
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
        paginationRequest: PaginationRequest,
        after: Identifier?,
    ): Either<InvitationError, Pagination<ChannelInvitation>> =
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
                    paginationRequest,
                    after ?: Identifier(0),
                )

            success(invitations)
        }

    override fun updateInvitation(
        channelId: Identifier,
        inviteId: Identifier,
        role: ChannelRole?,
        expirationDate: LocalDateTime?,
        user: User,
    ): Either<InvitationError, Unit> =
        transactionManager.run {
            if (expirationDate != null) {
                val expiryValidation = validateExpirationDate(expirationDate)
                if (expiryValidation is Failure) {
                    return@run expiryValidation
                }
            }

            if (role == ChannelRole.OWNER) {
                return@run Failure(InvitationError.OwnerInvitationNotAllowed)
            }

            val channel = channelRepository.findById(channelId) ?: return@run Failure(InvitationError.ChannelNotFound)

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

            channelInvitationRepository.save(
                invitation.copy(role = role ?: invitation.role, expiresAt = expirationDate ?: invitation.expiresAt),
            )

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

            if (invitation.inviter != user && channel.owner != user) {
                return@run Failure(InvitationError.UserCannotDeleteInvitation)
            }

            channelInvitationRepository.delete(invitation)

            success(Unit)
        }

    override fun getUserInvitations(
        userId: Identifier,
        user: User,
        sortRequest: SortRequest,
        paginationRequest: PaginationRequest,
        after: Identifier?,
    ): Either<InvitationError, Pagination<ChannelInvitation>> =
        transactionManager.run {
            if (userId != user.id) {
                return@run Failure(InvitationError.UserCannotAccessInvitation)
            }

            val sort = sortRequest.sortBy ?: DEFAULT_SORT

            if (!validSortFields.contains(sort)) {
                return@run Failure(InvitationError.InvalidSortField(sort, validSortFields.toList()))
            }

            val invitations =
                channelInvitationRepository.findByInvitee(
                    user,
                    ChannelInvitationStatus.PENDING,
                    sortRequest.copy(sortBy = sort),
                    paginationRequest,
                    after ?: Identifier(0),
                )

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
                channelRepository.addMember(channel, user, role)
            } else {
                channelInvitationRepository.save(invitation.reject())
            }

            success(Unit)
        }

    private fun validateExpirationDate(expirationDate: LocalDateTime): Either<InvitationError, Unit> {
        val minExpiration = LocalDateTime.now().plusMinutes(config.minChannelInvitationTTL.inWholeMinutes)
        val maxExpiration = LocalDateTime.now().plusHours(config.maxChannelInvitationTTL.inWholeHours)

        return when {
            expirationDate.isBefore(minExpiration) ->
                failure(
                    InvitationError.InvalidInvitationExpiration(
                        "Minimum expiration time is ${config.minChannelInvitationTTL.inWholeMinutes} minutes",
                    ),
                )
            expirationDate.isAfter(maxExpiration) ->
                failure(
                    InvitationError.InvalidInvitationExpiration(
                        "Maximum expiration time is ${config.maxChannelInvitationTTL.inWholeHours} hours",
                    ),
                )
            else -> success(Unit)
        }
    }

    override fun createImInvitation(expiration: LocalDateTime?): Either<InvitationError, ImInvitation> =
        transactionManager.run {
            val expires = expiration ?: LocalDateTime.now().plusMinutes(config.defaultImInvitationTTL.inWholeMinutes)
            val minExpiration = LocalDateTime.now().plusMinutes(config.minImInvitationTTL.inWholeMinutes)
            val maxExpiration = LocalDateTime.now().plusMinutes(config.maxImInvitationTTL.inWholeMinutes)

            when {
                expires.isBefore(minExpiration) -> return@run failure(
                    InvitationError.InvalidInvitationExpiration(
                        "Minimum expiration time is ${config.minImInvitationTTL.inWholeMinutes} minutes",
                    ),
                )
                expires.isAfter(maxExpiration) -> return@run failure(
                    InvitationError.InvalidInvitationExpiration(
                        "Maximum expiration time is ${config.maxImInvitationTTL.inWholeHours} hours",
                    ),
                )
            }

            val invitation =
                imInvitationRepository.save(
                    ImInvitation(expiresAt = expires),
                )

            success(invitation)
        }
}
