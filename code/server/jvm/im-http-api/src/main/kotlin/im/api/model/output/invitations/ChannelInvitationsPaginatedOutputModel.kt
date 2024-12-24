package im.api.model.output.invitations

import im.api.model.output.PaginationOutputModel
import im.domain.invitations.ChannelInvitation
import im.repository.pagination.Pagination

data class ChannelInvitationsPaginatedOutputModel(
    val invitations: List<ChannelInvitationOutputModel>,
    val pagination: PaginationOutputModel? = null,
) {
    companion object {
        fun fromDomain(invitations: Pagination<ChannelInvitation>): ChannelInvitationsPaginatedOutputModel =
            ChannelInvitationsPaginatedOutputModel(
                invitations = invitations.items.map { ChannelInvitationOutputModel.fromDomain(it) },
                pagination = PaginationOutputModel.fromPagination(invitations.info),
            )
    }
}
