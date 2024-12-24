import { UserOutputModel } from '../users/UserOutputModel';
import { ChannelOutputModel } from '../channels/ChannelOutputModel';
import { ChannelInvitationStatus } from '../../../Domain/invitations/ChannelInvitationStatus';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';

/**
 * Channel invitation output model
 *
 * @param id The unique identifier of the channel invitation.
 * @param channel The channel to which the user is invited.
 * @param invitee The user who is invited to the channel.
 * @param inviter The user who invited the invitee to the channel.
 * @param status The status of the invitation.
 * @param role The role of the user in the channel.
 * @param expiresAt The date and time when the invitation expires.
 */
export interface ChannelInvitationOutputModel {
    id: number;
    channel: ChannelOutputModel;
    invitee: UserOutputModel;
    inviter: UserOutputModel;
    status: ChannelInvitationStatus;
    role: ChannelRole;
    expiresAt: string;
}
