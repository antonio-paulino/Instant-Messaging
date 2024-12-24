import { ChannelRole } from '../../../Domain/channel/ChannelRole';

/**
 * Output model for channel member
 *
 * @param id The unique identifier of the member.
 * @param name The name of the member.
 * @param role The role of the member in the channel.
 */
export interface ChannelMemberOutputModel {
    id: number;
    name: string;
    role: ChannelRole;
}
