import { UserOutputModel } from '../users/UserOutputModel';
import { ChannelMemberOutputModel } from './ChannelMemberOutputModel';
import { ChannelRole } from '../../../Domain/channel/ChannelRole';

/**
 * Channel member output model
 *
 * @param id The unique identifier of the member.
 * @param name The name of the member.
 * @param defaultRole The default role of a user in the channel.
 * @param owner The owner of the channel.
 * @param isPublic Indicates whether the channel is public.
 * @param members The members of the channel
 */
export interface ChannelOutputModel {
    id: number;
    name: string;
    defaultRole: ChannelRole;
    owner: UserOutputModel;
    isPublic: boolean;
    members: ChannelMemberOutputModel[];
}
