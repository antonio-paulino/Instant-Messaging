import { Identifier } from '../wrappers/identifier/Identifier';
import { Name } from '../wrappers/name/Name';
import { ChannelRole } from './ChannelRole';
import { User, userFromDto } from '../user/User';
import { ChannelMember, channelMemberFromDto } from './ChannelMember';
import { ChannelCreationOutputModel } from '../../Dto/output/channels/ChannelCreationOutputModel';
import { ChannelOutputModel } from '../../Dto/output/channels/ChannelOutputModel';

/**
 * Represents a channel in the system.
 *
 * @param id The unique identifier of the channel.
 * @param name The name of the channel.
 * @param defaultRole The default role of a user in the channel.
 * @param owner The owner of the channel.
 * @param isPublic Indicates whether the channel is public.
 * @param createdAt The date and time when the channel was created.
 * @param members The members of the channel
 */
export interface Channel {
    id: Identifier;
    name: Name;
    defaultRole: ChannelRole;
    owner: User;
    isPublic: boolean;
    members: ChannelMember[];
}

export function channelFromDto(dto: ChannelOutputModel): Channel {
    return {
        id: new Identifier(dto.id),
        name: new Name(dto.name),
        defaultRole: dto.defaultRole,
        owner: userFromDto(dto.owner),
        isPublic: dto.isPublic,
        members: dto.members.map(channelMemberFromDto),
    };
}

export function channelFromCreation(
    dto: ChannelCreationOutputModel,
    name: Name,
    defaultRole: ChannelRole,
    owner: User,
    isPublic: boolean,
): Channel {
    return {
        id: new Identifier(dto.id),
        name,
        defaultRole,
        owner,
        isPublic,
        members: [{ id: owner.id, name: owner.name, role: ChannelRole.OWNER }],
    };
}
