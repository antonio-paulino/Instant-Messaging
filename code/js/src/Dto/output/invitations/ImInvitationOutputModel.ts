import { ImInvitationStatus } from '../../../Domain/invitations/ImInvitationStatus';

/**
 * Interface for invitation output model
 *
 * @param token The token of the invitation.
 * @param status The status of the invitation.
 * @param expiresAt The date and time when the invitation expires.
 */
export interface ImInvitationOutputModel {
    token: string;
    status: ImInvitationStatus;
    expiresAt: string;
}
