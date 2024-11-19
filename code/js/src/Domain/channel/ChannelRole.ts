/**
 * Represents a role of a user in a channel.
 */
export enum ChannelRole {
    OWNER = 'OWNER', // read-write
    MEMBER = 'MEMBER', // read-write
    GUEST = 'GUEST', // read-only
}
