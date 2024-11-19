import { PaginationOutputModel } from '../PaginationOutputModel';
import { ChannelInvitationOutputModel } from './ChannelInvitationOutputModel';

/**
 * Represents the output model for a paginated list of channel invitations.
 *
 * @param invitations The list of channel invitations.
 * @param pagination The pagination information.
 */
export interface ChannelInvitationsPaginatedOutputModel {
    invitations: ChannelInvitationOutputModel[];
    pagination: PaginationOutputModel;
}
