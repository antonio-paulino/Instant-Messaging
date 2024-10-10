package im.channel

/**
 * Represents the role of a user in a channel.
 *  - OWNER: can manage the channel and its members
 *  - MEMBER: can read and write messages
 *  - GUEST: can only read messages
 */
enum class ChannelRole {
    OWNER,
    MEMBER, // read-write
    GUEST // read-only
}