import { Channel, channelFromDto } from '../channel/Channel';
import { User, userFromDto } from '../user/User';
import { Identifier } from '../wrappers/identifier/Identifier';
import { ChannelInvitationStatus } from './ChannelInvitationStatus';
import { ChannelRole } from '../channel/ChannelRole';
import { ChannelInvitationCreationOutputModel } from '../../Dto/output/invitations/ChannelInvitationCreationOutputModel';
import { ChannelInvitationOutputModel } from '../../Dto/output/invitations/ChannelInvitationOutputModel';

/**
 * Represents a channel invitation.
 *
 * @param id The unique identifier of the channel invitation.
 * @param channel The channel to which the user is invited.
 * @param inviter The user who invited the invitee to the channel.
 * @param invitee The user who is invited to the channel.
 * @param status The status of the invitation.
 * @param role The role of the user in the channel.
 * @param expiresAt The date and time when the invitation expires.
 *
 */
export interface ChannelInvitation {
    id: Identifier;
    channel: Channel;
    inviter: User;
    invitee: User;
    status: ChannelInvitationStatus;
    role: ChannelRole;
    expiresAt: Date;
}

export function channelInvitationFromDto(
    dto: ChannelInvitationOutputModel,
): ChannelInvitation {
    return {
        id: new Identifier(dto.id),
        channel: channelFromDto(dto.channel),
        inviter: userFromDto(dto.inviter),
        invitee: userFromDto(dto.invitee),
        status: dto.status,
        role: dto.role,
        expiresAt: new Date(dto.expiresAt),
    };
}

export function channelInvitationFromCreation(
    dto: ChannelInvitationCreationOutputModel,
    channel: Channel,
    invitee: User,
    expiresAt: Date,
    role: ChannelRole,
): ChannelInvitation {
    return {
        id: new Identifier(dto.id),
        channel,
        inviter: channel.owner,
        invitee,
        status: ChannelInvitationStatus.PENDING,
        role,
        expiresAt,
    };
}
