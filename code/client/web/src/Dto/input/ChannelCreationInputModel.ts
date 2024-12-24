/**
 * Input model for creating a channel
 *
 * @param name The name of the channel.
 * @param defaultRole The default role of a user in the channel.
 * @param isPublic Indicates whether the channel is public.
 */
export interface ChannelCreationInputModel {
    name: string;
    defaultRole: string;
    isPublic: boolean;
}
