import { Identifier } from '../wrappers/identifier/Identifier';
import { Name } from '../wrappers/name/Name';
import { ChannelRole } from './ChannelRole';
import { ChannelMemberOutputModel } from '../../Dto/output/channels/ChannelMemberOutputModel';

/**
 * Represents a channel member.
 *
 * @param id The unique identifier of the member.
 * @param name The name of the member.
 * @param role The role of the member in the channel.
 */
export interface ChannelMember {
    id: Identifier;
    name: Name;
    role: ChannelRole;
}

export function channelMemberFromDto(dto: ChannelMemberOutputModel): ChannelMember {
    return {
        id: new Identifier(dto.id),
        name: new Name(dto.name),
        role: dto.role,
    };
}
